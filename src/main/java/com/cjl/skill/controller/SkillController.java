package com.cjl.skill.controller;

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DigestUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.cjl.skill.cache.LocalCache;
import com.cjl.skill.mq.MQProducer;
import com.cjl.skill.pojo.Activity;
import com.cjl.skill.pojo.ActivityProduct;
import com.cjl.skill.pojo.Address;
import com.cjl.skill.pojo.Order;
import com.cjl.skill.pojo.Product;
import com.cjl.skill.pojo.User;
import com.cjl.skill.service.ActivityProductService;
import com.cjl.skill.service.ActivityService;
import com.cjl.skill.service.OrderService;
import com.cjl.skill.service.ProductService;
import com.cjl.skill.util.AckMessage;
import com.cjl.skill.util.ConstantPrefixUtil;
import com.cjl.skill.util.JsonUtil;
import com.cjl.skill.util.RequestHelper;
import com.cjl.skill.vcode.VerifyCode;
import com.cjl.skill.vo.ProductActVo;

@Controller
public class SkillController {
	@Autowired
	private OrderService orderService;

	@Autowired
	private ProductService productService;

	@Autowired
	private ActivityService activityService;

	@Autowired
	private ActivityProductService activityProductService;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Autowired
	private CuratorFramework client;
	
	@Autowired
	private MQProducer producer;

	@Autowired
	private VerifyCode verifyCode;

	/**
	 * 返回整个中控页面
	 * 
	 * @param model
	 * @return
	 */
	@GetMapping("/skill")
	public String skillPage(Model model) {
		// 获取所有商品
		List<Product> products = productService.getAll();

		// 视图对象（里面多了一个属性）
		List<ProductActVo> proActs = new ArrayList<ProductActVo>();
		// 初始化商品基本信息
		for (Product p : products) {
			ProductActVo pav = new ProductActVo();
			pav.setId(p.getId());
			// 默认就是0
			pav.setActId(0);
			pav.setProductName(p.getProductName());
			pav.setCreateTime(p.getCreateTime());
			pav.setId(p.getId());
			pav.setPrice(p.getPrice());
			pav.setStatus(p.getStatus());
			pav.setStock(p.getStock());
			proActs.add(pav);
		}

		// 获取参加该活动的商品，为便于测试，默认活动id是1
		List<ActivityProduct> aps = activityProductService.getByActId(1);
		// 设置活动标识
		for (ActivityProduct activityProduct : aps) {
			for (ProductActVo v : proActs) {
				if (activityProduct.getProductId() == v.getId()) {
					v.setActId(activityProduct.getActivityId());
					break;
				}
			}
		}
		// 添加vo对象到请求域，页面默认显示参加活动的商品进行业务测试
		model.addAttribute("proActs", proActs);

		// 获取秒杀活动信息,默认活动是1
		Activity one = activityService.getOne(1);

		model.addAttribute("activity", one);

		return "skill";
	}

	/**
	 * 秒杀接口1.0
	 */
	@PostMapping("/skill")
	public @ResponseBody Object skill(int productId) {

		// 做商品售完判断，拦截无效的请求
		if (LocalCache.SOLD_OUT_FLAGS.get(String.valueOf(productId)) != null) {
			return new AckMessage<>(603, "商品已经抢完了");
		}

		// 后台校验秒杀时间
		if (!validSkillTime(productId)) {
			return AckMessage.error("time is error");
		}

		// 验证参数
		if (productId <= 0) {
			return AckMessage.illegalArgs();
		}

		// 验证是否登陆
		User user = getLoginUser();
		if (user == null) {
			return AckMessage.unauthorized();
		}

		// 是否有默认收货地址
		Address address = getUserDefaultAddress(user);
		if (address == null) {
			return new AckMessage<>(601, "没有默认收货地址");
		}

		// 一个人一个商品只能下一次单
		String json = stringRedisTemplate.opsForValue()
				.get(ConstantPrefixUtil.REDIS_ORDER_SUCCESS_FLAG_PREFIX + user.getId() + ":" + productId);
		if (json != null) {
			return new AckMessage<>(201, "亲，该商品已经秒杀过了");
		}

		// 设置redis订单排队标记，分布式锁功能，保证同一用户同一商品只能秒杀成功一次，没有抢到锁的同学，说明前面自己已经在排队了
		// 设置超时时间防止死锁
		if (!stringRedisTemplate.opsForValue().setIfAbsent(
				ConstantPrefixUtil.REDIS_ORDER_QUEUE_FLAG_PREFIX + user.getId() + ":" + productId, "queue", 60,
				TimeUnit.SECONDS)) {
			return new AckMessage<>(200, "亲，您正在排队中，请耐心等待哦");
		}

		// redis来减轻数据库的压力：10w QPS，原子减，串行执行
		Long result = stringRedisTemplate.opsForValue().decrement(ConstantPrefixUtil.SKILL_PRODUCT_PREFIX + productId);
		if (result < 0) {
			// 还原负数
			stringRedisTemplate.opsForValue().increment(ConstantPrefixUtil.SKILL_PRODUCT_PREFIX + productId);
			// 添加售完标记
			LocalCache.SOLD_OUT_FLAGS.put(String.valueOf(productId), true);

			// 创建zk售完标记节点
			createZkNode(productId);

			return new AckMessage<>(603, "商品已经抢完了");
		}

		// 商品是否存在
		Product product = productService.getById(productId);
		if (product == null) {
			return new AckMessage<>(602, "商品不存在");
		}

		// 检查库存
		if (product.getStock() < 1) {
			return new AckMessage<>(603, "商品已经抢完了");
		}

		// 同步生成订单
		// return createOrder(product, address, user);

		// 异步下单，成功排队
		return createAsyncOrder(product, address, user);

	}

	/**
	 * 秒杀接口2.0
	 */
	@PostMapping("/skill/{token}")
	public @ResponseBody Object skillSafe(int productId, @PathVariable String token) {

		// 做商品售完判断，拦截无效的请求
		if (LocalCache.SOLD_OUT_FLAGS.get(String.valueOf(productId)) != null) {
			return new AckMessage<>(603, "商品已经抢完了");
		}

		// 后台校验秒杀时间
		if (!validSkillTime(productId)) {
			return AckMessage.error("time is error");
		}

		// 验证参数
		if (productId <= 0) {
			return AckMessage.illegalArgs();
		}

		// 验证是否登陆
		User user = getLoginUser();
		if (user == null) {
			return AckMessage.unauthorized();
		}

		//验证用户token
		boolean check = checkToken(user, productId, token);
		if (!check) {
			return AckMessage.error("token错误");
		}

		// 是否有默认收货地址
		Address address = getUserDefaultAddress(user);
		if (address == null) {
			return new AckMessage<>(601, "没有默认收货地址");
		}

		// 一个人一个商品只能下一次单
		String json = stringRedisTemplate.opsForValue()
				.get(ConstantPrefixUtil.REDIS_ORDER_SUCCESS_FLAG_PREFIX + user.getId() + ":" + productId);
		if (json != null) {
			return new AckMessage<>(201, "亲，该商品已经秒杀过了");
		}

		// 设置redis订单排队标记，分布式锁功能，保证同一用户同一商品只能秒杀成功一次，没有抢到锁的同学，说明前面自己已经在排队了
		// 设置超时时间防止死锁
		if (!stringRedisTemplate.opsForValue().setIfAbsent(
				ConstantPrefixUtil.REDIS_ORDER_QUEUE_FLAG_PREFIX + user.getId() + ":" + productId, "queue", 60,
				TimeUnit.SECONDS)) {
			return new AckMessage<>(200, "亲，您正在排队中，请耐心等待哦");
		}

		// redis来减轻数据库的压力：10w QPS，原子减，串行执行
		Long result = stringRedisTemplate.opsForValue().decrement(ConstantPrefixUtil.SKILL_PRODUCT_PREFIX + productId);
		if (result < 0) {
			// 还原负数
			stringRedisTemplate.opsForValue().increment(ConstantPrefixUtil.SKILL_PRODUCT_PREFIX + productId);
			// 添加售完标记
			LocalCache.SOLD_OUT_FLAGS.put(String.valueOf(productId), true);

			// 创建zk售完标记节点
			createZkNode(productId);

			return new AckMessage<>(603, "商品已经抢完了");
		}

		// 商品是否存在
		Product product = productService.getById(productId);
		if (product == null) {
			return new AckMessage<>(602, "商品不存在");
		}

		// 检查库存
		if (product.getStock() < 1) {
			return new AckMessage<>(603, "商品已经抢完了");
		}

		// 同步生成订单
		// return createOrder(product, address, user);

		// 异步下单，成功排队
		return createAsyncOrder(product, address, user);

	}

	// 查询订单接口
	@GetMapping("/order/query")
	public @ResponseBody Object getOrderResult(int productId) {
		// 验证参数
		if (productId <= 0) {
			return AckMessage.illegalArgs();
		}

		// 验证是否登陆
		User user = getLoginUser();
		if (user == null) {
			return AckMessage.unauthorized();
		}
		// 查看是否在排队
		if (stringRedisTemplate.opsForValue()
				.get(ConstantPrefixUtil.REDIS_ORDER_QUEUE_FLAG_PREFIX + user.getId() + ":" + productId) != null) {
			return AckMessage.ok("queue");
		}
		// 查看是否下单成功
		try {
			// 使用缓存挡住
			String json = stringRedisTemplate.opsForValue()
					.get(ConstantPrefixUtil.REDIS_ORDER_SUCCESS_FLAG_PREFIX + user.getId() + ":" + productId);
			return AckMessage.ok(json);
		} catch (Exception e) {
			e.printStackTrace();
			// 查数据库
			Order order = orderService.getOrderByUserAndProductId(user.getId(), productId);
			// 返回结果
			return AckMessage.ok(order);
		}
	}

	// 异步下单
	private Object createAsyncOrder(Product p, Address address, User user) {
		Order record = new Order();
		record.setNote("秒杀下单测试");
		record.setPrice(p.getPrice());
		record.setProductId(p.getId());
		record.setQuantity(1);
		record.setUserId(user.getId());
		record.setSum(p.getPrice());
		record.setStatus("待付款");
		try {
			//发送消息
			producer.sendMessage("order_group", "order_topic", "order_tag", JsonUtil.obj2String(record).getBytes());
			System.out.println("send order message success.");
			return new AckMessage<>(200, "排队中。。。。。。。。。。");
		} catch (Exception e) {
			e.printStackTrace();
			// 异常情况，退库存
			stringRedisTemplate.opsForValue().increment(ConstantPrefixUtil.SKILL_PRODUCT_PREFIX + p.getId());
			// 清除售完标记
			LocalCache.SOLD_OUT_FLAGS.remove(String.valueOf(p.getId()));
			// 删除节点标记
			try {
				if(client.checkExists().forPath(ConstantPrefixUtil.ZK_SOLD_OUT_PRODUCT_ROOT_PATH + "/" + p.getId())!=null) {
					client.delete().forPath(ConstantPrefixUtil.ZK_SOLD_OUT_PRODUCT_ROOT_PATH + "/" + p.getId());
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			// 清除订单排队标记
			stringRedisTemplate
					.delete(ConstantPrefixUtil.REDIS_ORDER_QUEUE_FLAG_PREFIX + user.getId() + ":" + p.getId());
			return AckMessage.error("下单失败，发送消息超时");
		}
	}

	// 登录用户
	@PostMapping("/login")
	public @ResponseBody Object doLogin() {
		User user = new User(1, "cjl");
		HttpSession session = RequestHelper.getSession();
		// 登录成功向session写入数据
		session.setAttribute("user", user);
		// 无侵入性
		// redis set key=jssessionid = new User()
		// response.addCoolie()

		System.out.println("server port : " + RequestHelper.getRequest().getLocalPort()
				+ ". create session, sessionId is:" + session.getId());
		return AckMessage.ok();
	}

	// 用户登出
	@PostMapping("/logout")
	public @ResponseBody Object logout() {
		HttpSession session = RequestHelper.getSession();
		// 过期、失效
		session.invalidate();
		return AckMessage.ok();
	}

	// 刷新指定商品的库存 restful接口风格，可读性好，语义化
	@GetMapping("/refresh/{id}/stock")
	public @ResponseBody Object refreshStock(@PathVariable int id) {
		try {
			Product p = productService.getById(id);
			return AckMessage.ok(p.getStock());
		} catch (Exception e) {
			e.printStackTrace();
			return AckMessage.error();
		}
	}

	/**
	 * 下单操作
	 */
	private Object createOrder(Product p, Address address, User user) {
		Order record = new Order();
		record.setNote("秒杀下单测试");
		record.setPrice(p.getPrice());
		record.setProductId(p.getId());
		record.setQuantity(1);
		record.setUserId(user.getId());
		record.setSum(p.getPrice());
		record.setStatus("待付款");
		try {
			orderService.createSkillOrder(record);
			return AckMessage.ok();
		} catch (Exception e) {
			// 异常情况，退库存
			stringRedisTemplate.opsForValue().increment(ConstantPrefixUtil.SKILL_PRODUCT_PREFIX + p.getId());
			// 清除售完标记
			LocalCache.SOLD_OUT_FLAGS.remove(String.valueOf(p.getId()));
			// 删除节点标记
			try {
				client.delete().forPath(ConstantPrefixUtil.ZK_SOLD_OUT_PRODUCT_ROOT_PATH + "/" + p.getId());
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			return AckMessage.error(e.getMessage());
		}
	}

	/**
	 * 获取默认收货地址
	 */
	private Address getUserDefaultAddress(User user) {
		// 登录之后，把用户的默认地址放到缓存中，一般是redis中
		return new Address(1, "中国", user.getId(), true);
	}

	/**
	 * 获取登陆用户
	 */
	private User getLoginUser() {
		HttpSession session = RequestHelper.getSession();
		Object user = session.getAttribute("user");
		if (user != null) {
			return (User) user;
		} else {
			return null;
		}
	}

	/**
	 * 校验秒杀时间
	 * 
	 * @param productId
	 * @return
	 */
	private boolean validSkillTime(int productId) {
		// 优化第一步，把时间放到缓存中，具体的放到redis中

		Activity one = activityService.getOne(productId);
		long start = one.getStartTime().getTime();
		long end = one.getEndTime().getTime();
		long now = System.currentTimeMillis();
		if (now < start || now > end) {
			return false;
		} else {
			return true;
		}
	}

	// 创建zknode，用于同步jvm缓存
	private void createZkNode(int productId) {
		try {
			// 没有就创建
			if (client.checkExists()
					.forPath(ConstantPrefixUtil.ZK_SOLD_OUT_PRODUCT_ROOT_PATH + "/" + productId) == null) {
				client.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT)
						.withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
						.forPath(ConstantPrefixUtil.ZK_SOLD_OUT_PRODUCT_ROOT_PATH + "/" + productId);
			} else {
				// 有人退单了，后面的线程又抢掉了这个库存，之后；注意这个细节。
				client.setData().forPath(ConstantPrefixUtil.ZK_SOLD_OUT_PRODUCT_ROOT_PATH + "/" + productId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取令牌
	 */
	@GetMapping(value = "/token")
	@ResponseBody
	public AckMessage getSkillToken(String productId, String verifyCode, HttpServletRequest request) {
		//计数器限流算法，例如限制接口1分钟最多访问5次
		String key = ConstantPrefixUtil.REDIS_SKILL_LIMIT_URL_FLAG_PREFIX+request.getRequestURI().toString();
		Long requestNum = stringRedisTemplate.opsForValue().increment(key);
		if (requestNum == 1) {
			stringRedisTemplate.expire(key, 60, TimeUnit.SECONDS);
		} else if (requestNum > 5) {
			return AckMessage.ok("流量限制，请稍后再试");
		}

		User user = getLoginUser();
		if (user == null) {
			return AckMessage.unauthorized();
		}
		
		// 校验验证码
		boolean check = checkVerifyCode(user, productId, verifyCode);
		if (!check) {
			return AckMessage.vcodeError();
		}
		
		String token = createSkillToken(user, productId);
		
		return AckMessage.ok(token);
	}

	/**
	 * 校验验证码
	 */
	private boolean checkVerifyCode(User user, String productId, String verifyCode) {
		if (user == null || StringUtils.isEmpty(verifyCode)) {
			return false;
		}
		String verifyCodeRedisKey = ConstantPrefixUtil.REDIS_VCODE_FLAG_PREFIX + user.getId() + ":" + productId;
		String realCode = stringRedisTemplate.opsForValue().get(verifyCodeRedisKey);
		if (StringUtils.isEmpty(realCode) || !verifyCode.equals(realCode)) {
			return false;
		}
		//删除验证码
		stringRedisTemplate.delete(verifyCodeRedisKey);
		return true;
	}

	/**
	 * 创建token
	 */
	private String createSkillToken(User user, String productId) {
		if (user == null || productId == null) {
			return null;
		}
		String token = DigestUtils.sha1DigestAsHex(UUID.randomUUID().toString());
		stringRedisTemplate.opsForValue().set(
				ConstantPrefixUtil.REDIS_SKILL_TOKEN_FLAG_PREFIX + user.getId() + ":" + productId, token, 60,
				TimeUnit.SECONDS);
		return token;
	}

	/**
	 * 验证token
	 */
	private boolean checkToken(User user, int productId, String token) {
		if (user == null || token == null || productId <= 0) {
			return false;
		}
		String realToken = stringRedisTemplate.opsForValue()
				.get(ConstantPrefixUtil.REDIS_SKILL_TOKEN_FLAG_PREFIX + user.getId() + ":" + productId);
		boolean b = token.equals(realToken);
		// 验证完token删除
		stringRedisTemplate.delete(ConstantPrefixUtil.REDIS_SKILL_TOKEN_FLAG_PREFIX + user.getId() + ":" + productId);
		return b;
	}

	/**
	 * 获取验证码
	 */
	@GetMapping(value = "/verifyCode")
	@ResponseBody
	public AckMessage getSkillVerifyCod(int productId, HttpServletResponse response) {
		User user = getLoginUser();
		if (user == null) {
			return AckMessage.unauthorized();
		}
		try {
			BufferedImage image = verifyCode.createVerifyCode(user, productId);
			OutputStream out = response.getOutputStream();
			ImageIO.write(image, "JPEG", out);
			out.flush();
			out.close();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return AckMessage.error("生成验证码失败");
		}
	}
}
