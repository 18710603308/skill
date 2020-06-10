<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>秒杀</title>
<link href="/css/countdown.css" rel="stylesheet">
<link href="/css/globle.css" rel="stylesheet">
<link href="/calendar/calendar.min.css" rel="stylesheet">
<script type="text/javascript" src="/js/jquery-1.8.3.min.js"></script>
<script type="text/javascript" src="/calendar/calendar.min.js"></script>
<script type="text/javascript" src="/js/common.js"></script>

</head>
<body>
	<div style="width: 1200px; height:auto; text-align: center; margin: 0 auto; margin-top: 20px;">
		<!-- 活动后台管理 -->
		<div style="float: left; width: 33%; text-align: center; margin-top: 60px; border: 1px solid black;">
			<h3>秒杀活动管理</h3>
			<form id="actForm">
				<input type="hidden" name="id" value="${activity.id}"/>
				<p><span>活动名称：</span><input type="text" name="activityName" value="${activity.activityName}"/></p>
				<p>
					<span>活动商品：</span>
					<c:forEach var="p" items="${proActs}">
						<input <c:if test="${p.actId eq activity.id }">checked</c:if> type="checkbox" name="pids" value="${p.id}" />
						${p.productName}
					</c:forEach>
				</p>
				<p>
					<span>开始时间：</span>
					<input type="text" name="startTime" value='<fmt:formatDate value="${activity.startTime }" pattern="yyyy-MM-dd HH:mm:ss"/>' id="date1" class="select-text">
				</p>
				<p>
					<span>结束时间：</span>
					<input type="text" name="endTime" value='<fmt:formatDate value="${activity.endTime }" pattern="yyyy-MM-dd HH:mm:ss"/>' id="date2" class="select-text">
				</p>
				<p>
					<input type="button" value="保存活动" onclick="addActivity()" />
				</p>
				<script type="text/javascript">
					var startOptb = {
				                'targetId':'date1',//时间写入对象的id
				                'triggerId':['date1'],//触发事件的对象id
				                'alignId':'date1',//日历对齐对象
				                'format':'-',//时间格式 默认'YYYY-MM-DD HH:MM:SS'
				                'min':'2010-09-20 10:00:00',//最小时间
				                'max':'2020-12-31 23:59:59'//最大时间
				    }
					var startOpte = {
			                'targetId':'date2',//时间写入对象的id
			                'triggerId':['date2'],//触发事件的对象id
			                'alignId':'date2',//日历对齐对象
			                'format':'-',//时间格式 默认'YYYY-MM-DD HH:MM:SS'
			                'min':'2010-09-20 10:00:00',//最小时间
			                'max':'2020-12-31 23:59:59'//最大时间
			    	}
				    xvDate(startOptb);
					xvDate(startOpte);
	
					var act = $('#actForm').serialize();
					console.log(act);
					function addActivity(){
						$.ajax({
							url : '/admin/skill/activity',
							type : 'post',
							data: $('#actForm').serialize(),
							success : function(data) {
								if (data.status == 200) {
									alert('成功')
									window.location.href="http://localhost/skill"
								} else{
									alert('失败')
								}
							}
						})
					}
				</script>
			</form>
		</div>
		<!-- 秒杀页面 -->
		<div style="float: left;width: 33%; text-align: center;">
			<h3>秒杀商品页面</h3>
			<!-- 倒计时 -->
			<div class="se-kl">
		        <div class="se-cn">倒计时</div>
		       <!--  <div class="se-en">COUNT DOWN</div> -->
		        <i class="se-io"></i>
		        <div id="divSecSkillInfo" class="se-info">活动已经结束</div>
		        <div class="se-count">
		            <div class="se-day"></div>
		            <div class="se-hour"><span class="se-txt">00</span></div>
		            <div class="se-min"><span class="se-txt">00</span></div>
		            <div class="se-sec"><span class="se-txt">00</span></div>
		        </div>
		    </div>
			<!-- 商品提交表单 -->
			<c:forEach var="p" items="${proActs}">
				<c:if test="${p.actId eq activity.id}">
					<form id="skillForm_${p.id}" action="/skill" method="post">
						<input type="hidden" name="productId" value="${p.id }" /> 
						<input type="hidden" name="userId" value="1" />
						<table style="width: 100%">
							<tr>
								<td>名称：${p.productName }</td>
							</tr>
							<tr>
								<td>价格：${p.price }元</td>
							</tr>
							<tr>
								<td>库存：<span id="skillForm_${p.id}_stock">${p.stock}</span></td>
							</tr>
							<tr>
								<td>
								<input class="cls_btnSecKill" id="btnSecKill" type="button" value="秒杀" onclick="getSkillToken(${p.id})" />
								<input id="verifyCode${p.id}"  size="6" maxlength="6"  style="display:none" />
								<img id="verifyCodeImg${p.id}" align="bottom" width="80" height="29"  style="display:none" onclick="refreshVerifyCode(${p.id})"/>
								</td>
							</tr>
						</table>
					</form>
				</c:if>
			</c:forEach>
			<script type="text/javascript">
		      	//开始时间
				var startTime = javaTojsTime(new Date("${activity.startTime}"))
				console.log(startTime)
				 //结束时间
		        var endTime = javaTojsTime(new Date("${activity.endTime}"))
				console.log(endTime)
				
				//定义开始、结束倒计时
				var starterTimer,endTimer,now=new Date()
				
				if(startTime>now){
					starterTimer = setInterval("run()", 1000)
				}else if(now>=startTime && now<=endTime){
					//创建一个结束倒计时
					endTimer  = setInterval("run()", 1000);
				}else{
					changeBtnAndText("cls_btnSecKill","活动已经结束",false)
				}
		
				//倒计时
		        function run() {
		        	var nowDate = new Date();
		            console.log(nowDate)
		        	
		        	if(startTime>nowDate){//秒杀未开始
		        		getDate(startTime);
						//运行开始倒计时
						changeBtnAndText("cls_btnSecKill","距离开始还剩",false)
					}else if(nowDate>=startTime && nowDate<=endTime){//秒杀进行中
						getDate(endTime);
						changeBtnAndText("cls_btnSecKill","距离结束还剩",true)
						//停止开始定时器
						if(starterTimer){
							clearInterval(starterTimer)
						}
						//判断是否有结束定时器，没有就创建新的
						if(!endTimer){
							endTimer  = setInterval("run()", 1000);
						}
					}else{//秒杀结束
						changeBtnAndText("cls_btnSecKill","活动已经结束",false)
						//停止结束定时器
						if(endTimer){
							clearInterval(endTimer)
						}
					}
		        }
				//更新按钮状态以及文字
				function changeBtnAndText(className,skillTipText,isStart){
					document.getElementById("divSecSkillInfo").innerHTML = skillTipText
					var objs = document.getElementsByClassName(className);
				    for (var i = 0; i < objs.length; i++) {
				    	if(isStart){
				    		objs[i].removeAttribute("disabled")
				    		objs[i].setAttribute("style","background-color:#e83632;")
						}else{
							objs[i].setAttribute("disabled",true)
				    		objs[i].removeAttribute("style")
						}
				    }
				}
				
				//更新倒计时时分秒
		        function getDate(enddate) {
		            var oDate = new Date(); //获取日期对象
		
		            var nowTime = oDate.getTime(); //现在的毫秒数
		            var enddate = new Date(enddate);
		            var targetTime = enddate.getTime(); // 截止时间的毫秒数
		            var second = Math.floor((targetTime - nowTime) / 1000); //截止时间距离现在的秒数
		
		            var day = Math.floor(second / 24 * 60 * 60); //整数部分代表的是天；一天有24*60*60=86400秒 ；
		            second = second % 86400; //余数代表剩下的秒数；
		            var hour = Math.floor(second / 3600); //整数部分代表小时；
		            second %= 3600; //余数代表 剩下的秒数；
		            var minute = Math.floor(second / 60);
		            second %= 60;
		            var spanH = $('.se-txt')[0];
		            var spanM = $('.se-txt')[1];
		            var spanS = $('.se-txt')[2];
		
		            spanH.innerHTML = tow(hour);
		            spanM.innerHTML = tow(minute);
		            spanS.innerHTML = tow(second);
		        }
		
		        function tow(n) {
		            return n >= 0 && n < 10 ? '0' + n : '' + n;
		        }
		        
		      	//获取秒杀token
		        function getSkillToken(productId){
		        	$.ajax({
		        		url:"/token",
		        		type:"GET",
		        		data:{
		        			productId:productId,
		        			verifyCode:$("#verifyCode"+productId).val()
		        		},
		        		success:function(data){
		        			if(data.status == 200){
		        				let token = data.data;
		        				//拿着token去秒杀
		        				skillSafe(productId,token);
		        			}else{
		        				alert(data.message);
		        			}
		        		}
		        	});
		        }

		        //秒杀接口2.0
		        function skillSafe(id,token){			        
		        	$.ajax({
		        		url:"/skill/"+token,
		        		type:"POST",
		        		data:{
		        			productId:id
		        		},
		        		success:function(data){
		        			if(data.status == 201){
								alert(data.message)
							}
							else if (data.status == 200) {
								//开始查询订单
								queryOrder(id)
								//弹出友好排队提示
								alert(data.message)
							}else if(data.status == 401){
								//未登录
								alert('未登录错误，请登录')
							} 
							else if(data.status == 501){
								alert(data.message)
								refreshStock(id)
							}else{
								alert(data.message)
							}
		        		}
		        	});
		        }

		        //秒杀接口1.0
				function skill(id){
					$.ajax({
						url : '/skill',
						type : 'post',
						data: "productId="+id,
						success : function(data) {
							if(data.status == 201){
								alert(data.message)
							}
							else if (data.status == 200) {
								//开始查询订单
								queryOrder(id)
								//弹出友好排队提示
								alert(data.message)
								
								//alert('秒杀成功，请在30分钟内完成支付')
								//location.href='/skill'
								
							}else if(data.status == 401){
								//未登录
								alert('未登录错误，请登录')
							} 
							else if(data.status == 501){
								alert(data.message)
								refreshStock(id)
							}else{
								alert(data.message)
							}
						}
					})
				}
				//刷新库存接口
				function refreshStock(id){
					$.ajax({
						url : '/refresh/'+id+'/stock',
						type : 'get',
						success : function(data) {
							if (data.status == 200) {
								//alert('刷新库存成功')
								document.getElementById("skillForm_"+id+"_stock").innerHTML = data.data
							}else {
								alert(data.message)
							}
						}
					})
				}
				//定义一个订单查询定时器
				var ordertimer
				//查询订单
				function queryOrder(id){
					$.ajax({
						url : '/order/query',
						type : 'get',
						data: "productId="+id,
						success : function(data) {
							if (data.status == 200 && data.data == 'queue') {
								//继续查询，间隔1秒
								if(!ordertimer)
									ordertimer = setInterval("queryOrder("+id+")", 1000)
							}else if(data.status == 200 && data.data){
								//清除定时器
								if(ordertimer){
									clearInterval(ordertimer)
									ordertimer=null
								}
								//下单成功
								console.log(data.data)
								//局部刷新库存，仅用于测试skillForm_${p.id}_stock
								let domStock = document.getElementById("skillForm_"+id+"_stock")
								domStock.innerHTML = parseInt(domStock.innerHTML) - 1
								alert('下单成功，正跳转至支付页面。。。。。')
							}else{
								//清除定时器
								if(ordertimer){
									clearInterval(ordertimer)
									ordertimer=null
								}
								alert('下单失败')
							}
						}
					})
				}
				//刷新验证码
				function refreshVerifyCode(id){
					$("#verifyCodeImg"+id).attr("src", "/verifyCode?productId="+id+"&timestamp="+new Date().getTime());
				}
			</script>
		
			<!-- 用户登录管理 -->
			<div style="margin-top: 30px;">
				<a href="javascript:login();">登录</a>&nbsp;
				<a href="javascript:logout();">登出</a>&nbsp;
				<script type="text/javascript">
					//登录接口
					function login(){
						$.ajax({
							url : '/login',
							type : 'post',
							success : function(data) {
								if (data.status == 200) {
									$('[id^="verifyCode"]').show()
									$('[id^="verifyCodeImg"]').show()
									<c:forEach var="p" items="${proActs}">
										<c:if test="${p.actId eq activity.id}">
											//获取验证码
											refreshVerifyCode(${p.id});
										</c:if>
									</c:forEach>
									alert('登录成功')
								}else {
									alert(data.message)
								}
							}
						})
					}
					//登出
					function logout(){
						$.ajax({
							url : '/logout',
							type : 'post',
							success : function(data) {
								if (data.status == 200) {
									$('[id^="verifyCode"]').hide()
									$('[id^="verifyCodeImg"]').hide()
									alert('登出成功')
								}else {
									alert(data.message)
								}
							}
						})
					}
				</script>
			</div>
		</div>
		<!-- 压测管理 -->
		<div style="float: right; width: 33%; text-align: center; margin-top: 60px; border: 1px solid black; font-size: 14px;">
			<h3>压测管控台</h3>
			<div style="margin-bottom: 60px">
				<a href="javascript:initData();">初始化商品数据</a>&nbsp;
				<a href="javascript:loadStock();">预热商品库存数据</a>&nbsp;
				<a href="javascript:clearCache();">清除本地缓存数据</a><br/>
				<script type="text/javascript">
					function initData(){
						$.ajax({
							url : '/admin/skill/init',
							type : 'get',
							success : function(data) {
								if (data.status == 200) {
									location.href='/skill'
								} else{alert('失败')}
							}
						})
					}
					function loadStock(){
						$.ajax({
							url : '/admin/skill/load/stock',
							type : 'get',
							success : function(data) {
								if (data.status == 200) {
									//location.href='/skill'
								} else{alert('失败')}
							}
						})
					}
	
					function clearCache(){
						$.ajax({
							url : '/admin/skill/cache/clear',
							type : 'get',
							success : function(data) {
								if (data == 'ok') {
									location.href='/skill'
								} else{alert('失败')}
							}
						})
					}
				</script>
			</div>
			
			<h3>压测结果</h3>
			<div>
				<a href="javascript:report();">获取测试结果数据</a>
			</div>
			<div id="reports">
				
			</div>
			
			<script type="text/javascript">
				function report(){
					$.ajax({
						url : '/admin/skill/report',
						type : 'get',
						success : function(data) {
							if (data.status == 200) {
								let $rps = $('#reports')
								$rps.empty()
								for(let r of data.data){
									//拼接字符串html
									let str = "<p>商品名称："+r.productName+"</p>"+"<p>商品库存："+r.productStock+"</p>"+
									"<p style='margin-bottom: 30px'>商品订单："+r.orderCount+"</p>"
									$rps.append(str)
								}
							} else{
								alert(data.message)
							}
						}
					})
				}
			</script>
		</div>
	</div>
</body>
</html>