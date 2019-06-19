## Simple DNS Proxy 是一个可以进行域名映射的一个工具，支持通配符映射
 
### 快速开始
- 修改 win 系统dns服务器地址 (打开网络共享中心->更改适配器->本地或无线连接->选择Internet 版本4)
- 打开bin目录下可执行文件 debug-start-service.bat
- 编辑hosts 文件
 
### 以注册服务启动

- 修改服务名称 ，打开 myService.xml
    
    默认名称 ：SimpleDNSProxy 可以不用修改
    
      
    <configuration>
    <id>SimpleDNSProxy</id>
    <name>SimpleDNSProxy</name>
    <description>SimpleDNSProxy</description>
    <executable>java</executable>
    <arguments>-jar dnsproxy-1.0-SNAPSHOT.jar 8.8.8.8 53 --print</arguments>
    <logmode>rotate</logmode>
    </configuration>
   

   

- 执行 install-service.bat

- 注意事项

     杀毒软件可能会拦截，仅用于开发测试

    