package com.cjl.skill.vcode;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import com.cjl.skill.pojo.User;
import com.cjl.skill.util.ConstantPrefixUtil;

@Component
public class VerifyCode {
	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	
	/**
	 * 创建验证码
	 * jcl
	 * @param account
	 * @param productId
	 * @return
	 */
	public BufferedImage createVerifyCode(User user, int productId) {
		if (user == null) {
			return null;
		}
		int width = 80;
		int height = 32;
		// create the image
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		// set the background color
		g.setColor(new Color(0xDCDCDC));
		g.fillRect(0, 0, width, height);
		// draw the border
		g.setColor(Color.black);
		g.drawRect(0, 0, width - 1, height - 1);
		// create a random instance to generate the codes
		Random rdm = new Random();
		// make some confusion
		for (int i = 0; i < 50; i++) {
			int x = rdm.nextInt(width);
			int y = rdm.nextInt(height);
			g.drawOval(x, y, 0, 0);
		}
		// generate a random code
		String verifyCode = generateVerifyCode(rdm);
		g.setColor(new Color(0, 100, 0));
		g.setFont(new Font("Candara", Font.BOLD, 24));
		g.drawString(verifyCode, 8, 24);
		g.dispose();
		// 把验证码存到redis中
		Integer result = calc(verifyCode); // 12
		if (result == null) {
			return null;
		}
		stringRedisTemplate.opsForValue().set(ConstantPrefixUtil.REDIS_VCODE_FLAG_PREFIX+user.getId()+":"+productId,
				result.toString(),300,TimeUnit.SECONDS);
		// 输出图片
		return image;
	}

	private static Integer calc(String exp) {
		try {
			ScriptEngineManager manager = new ScriptEngineManager();
			ScriptEngine engine = manager.getEngineByName("JavaScript");
			return (Integer) engine.eval(exp);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static char[] ops = new char[] { '+', '-', '*' };

	/**
	 * + - *
	 */
	private String generateVerifyCode(Random rdm) {
		int num1 = rdm.nextInt(10);
		int num2 = rdm.nextInt(10);
		int num3 = rdm.nextInt(10);
		char op1 = ops[rdm.nextInt(3)];
		char op2 = ops[rdm.nextInt(3)];
		String exp = "" + num1 + op1 + num2 + op2 + num3;
		return exp;
	}

}
