# SMSS
通过字节码对比检测agent内存马.
# 快速开始
1. 下载smss-agent.jar和smss-engine.jar包或者clone项目运行`mvn clean package`打包
2. 运行`java -jar smss-engine.jar -p [检测项目进程号]`进行扫描.

帮助文档
```
usage: java -jar smss-engine.jar [-c] [-h] [-l] [-m <arg>] [-p <arg>] [-v]
 -c,--cleanup      cleanup memshell(清除内存码)
 -h,--help         print options information(打印帮助信息)
 -l,--list         list all java processes(列举本机上所有java进程)
 -m,--mode <arg>   scan mode: quick and full,default quick(扫描模式,quick:仅扫描所有java,sun等开头的包下的类,full:扫描所有类,默认quick)
 -p,--pid <arg>    scan jvm pid(待扫描应用的进程id)
 -v,--version      print the version of simpleMemShellScanner(打印版本)
```

# 扫描结果分析

扫描结果打印在应用的console里面.如下:

```
   _____   __  __   _____   _____ 
 / ____| |  \/  | / ____| / ____|
| (___   | \  / | | (___  | (___  
 \___ \  | |\/| |  \___ \  \___ \ 
 ____) | | |  | |  ____) | ____) |
|_____/  |_|  |_| |_____/ |_____/ 

[!] 发现注入的类：org/apache/shiro/web/servlet/ProxiedFilterChain
[!] 原始类保存在/Documents/Project/RuoYi/./danger-class-dump/org/apache/shiro/web/servlet/ProxiedFilterChain.original.java
[!] 注入类保存在/Documents/Project/RuoYi/./danger-class-dump/org/apache/shiro/web/servlet/ProxiedFilterChain.danger.java
```

扫描程序会将注入类和原始类保存在当前应用的`danger-class-dump`目录中.

# 测试

| JDK版本 | 中间件版本             | 操作系统         |                               memshell                               | 结果   | 备注               |
|:-------:|:-----------------:|:------:|:--------------------------------------------------------------------:|:------------------:|:------------------:|
| 11.0.17 | tomcat 8.5.8      | windows | 冰蝎/[weblogic_memshell](https://github.com/keven1z/weblogic_memshell) | 通过 |        |
| 1.8.0_341| tomcat 9.0.98     | windows |                         冰蝎/[weblogic_memshell](https://github.com/keven1z/weblogic_memshell)                        | 通过 |        |
| 1.8.0_231 | tomcat 7.0.96 | windows |                         冰蝎/[weblogic_memshell](https://github.com/keven1z/weblogic_memshell)                        | 通过 |        |
| 11.0.25 | Spring boot 2.5.15 | Mac |                          [weblogic_memshell](https://github.com/keven1z/weblogic_memshell)                        | 通过 |        |

