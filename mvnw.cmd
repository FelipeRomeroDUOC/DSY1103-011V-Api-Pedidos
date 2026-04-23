@ECHO OFF
SETLOCAL

SET "BASEDIR=%~dp0"
SET "WRAPPER_JAR=%BASEDIR%.mvn\wrapper\maven-wrapper.jar"

IF NOT "%JAVA_HOME%"=="" (
  SET "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
) ELSE (
  SET "JAVA_CMD=java.exe"
)

"%JAVA_CMD%" -Dmaven.multiModuleProjectDirectory="%BASEDIR:~0,-1%" -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
