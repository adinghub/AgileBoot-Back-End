@echo off
echo.
echo [信息] 使用Jar命令运行Web工程。
echo.

cd %~dp0
rem 模块目录已统一改为 healthtrail-admin，这里同步切换到新的构建产物目录。
cd ../healthtrail-admin/target

set JAVA_OPTS=-Xms256m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m

rem 统一使用重命名后的管理后台 jar，避免脚本与 Maven 产物名不一致。
java -Dspring.profiles.active=dev %JAVA_OPTS% -jar healthtrail-admin.jar

cd bin
pause
