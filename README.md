# pushForAndroid
一个 Android 端的消息推送模块，配合仓库中的 java 端代码可以进行消息推送


### 功能说明

此模块的功能主要有：

1. 与服务端建立 Socket 连接，接收服务端传递的消息
2. 根据分组信息确定是否要给用户推送消息
3. 对于离线的用户，会将要推送给他的消息保存在 mysql 数据库，然后以一定的频率查询数据库，将可以推送的消息（用户已上线）推送给用户并清除数据库的记录


### 使用的步骤

推送模块为 android Module，使用时首先将此 module 引入到工程中，然后在项目的 build.gradle 文件中依赖此 Module
implementation project(":module名称")

使用步骤:

> 调用 PushSocket.id(Context context, String account, String group)
>> 参数说明
>>
>> account 登录的账户
>>
>> group 用户的分组

> 调用 PushSocket.register()
>> 调用此方法后会开启 Service 服务，服务中会与后台建立 Socket 连接， 并将必要的信息，如唯一标识，用户名，用户的分组，电话号码（电话号码不是必要）传入后台, 开启线程用于监听后台传递过来的信息，并且注册一个广播用来进行通知展示

> 调用 PushSocket.unRegister()
>> 调用此方法后，会停止 Service 的运行，停止广播的监听，并且告诉后台，这台设备下线了，有消息请保存数据库，待上线后推送。

消息推送接口
<table>
	<tr>
	 <td>url</td>
	 <td>/cn-win/push</td>
	</tr>
	<tr>
	 <td>method</td>
	 <td>POST</td>
	</tr>
	<tr>
	 <td>param</td>
	 <td>notifyMethod</td>
	 <td>String</td>
	 <td>消息的提醒方式 noti 通知栏 app 应用弹窗</td>
	</tr>
	<tr>
	 <td></td>
	 <td>level</td>
	 <td>String</td>
	 <td>消息的类别 err warn info </td>
	</tr>
	<tr>
	 <td></td>
	 <td>group</td>
	 <td>String</td>
	 <td>需要推送的分组 all 表示全部</td>
	</tr>
	<tr>
	 <td></td>
	 <td>message</td>
	 <td>String</td>
	 <td>需要发送的消息</td>
	</tr>
</table>

eg:

      try {
            URL url = new URL("http://localhost:8888/cn-win/push");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-type", "application/json");
            OutputStream os = connection.getOutputStream();
            os.write("{\"notifyMethod\": \"noti\", \"level\": \"warn\", \"group\":\"all\",\"message\":\"test\"}".getBytes());
            os.flush();
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (200 == responseCode) {
                System.out.println("OK");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

### 实现说明

#### android 端

开启 Service 服务来建立 Socket 连接， 在 Service onCreate() 方法中进行 sokcet 连接的建立， 第一次建立连接的时候将身份标识相关的东西传递给后台，同时开启一个线程用来监听后台传递的数据。

另外，由于 Socket 每隔一段时间，就会发送新消息判断此链接是否还“活”着，因此，每隔一段时间还需要向后台发送“心跳”信息，以维持建立的 Socket 连接， 消息的定时发送主要是通过 Handler 的 postDelayed 方法实现。

关于消息的处理，则是在接收到消息后发送广播，在接收到的广播进行具体的消息展示。

#### java 端

在接收到建立的 Sokcet 连接之后，用一个 javabean 将此 socket 的相关信息（标识，账号，时间等）包装起来，放到一个 list 集合中， list 集合确保同一个标识的数据只存在一份。

推送消息的时候，首先根据分组筛选，然后将离线的数据保存起来，先发送给在线的满足要求的设备。同时，定时执行的任务每隔一定的时间，回去数据库中拉取未发送成功的数据，重新发送， 如发送成功则将此条记录删除， 并且，如果已经超过一天的信息，不会再次发送
