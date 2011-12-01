set CLASS=__class__
set CP="lib\Bambi.jar;lib\BambiCore.jar;lib\JDFLibJ-2.1.4a.jar;lib\JDFUtility-2.1.4a.jar;lib\activation-1.0.2.jar;lib\commons-fileupload-1.2.jar;lib\commons-io-2.0.jar;lib\commons-lang-2.5.jar;lib\commons-logging.jar;lib\jetty-continuation-7.2.0.v20101020.jar;lib\jetty-http-7.2.0.v20101020.jar;lib\jetty-io-7.2.0.v20101020.jar;lib\jetty-security-7.2.0.v20101020.jar;lib\jetty-server-7.2.0.v20101020.jar;lib\jetty-servlet-7.2.0.v20101020.jar;lib\jetty-servlets-7.2.0.v20101020.jar;lib\jetty-util-7.2.0.v20101020.jar;lib\jsp-api.jar;lib\log4j-1.2.8.jar;lib\mailapi.jar;lib\servlet-api.jar;lib\xercesImpl.jar;lib\xml-apis.jar" 

set START_VAR=--StartClass=%CLASS% --StartParams=start
set STOP_VAR=--StopClass=%CLASS% --StopParams=stop
set SERVICE_LOGS=--LogPath=C:\BambiData\logs --LogLevel=Info --StdOutput=auto --StdError=auto

"__launcher__" __serviceid__ --DisplayName="__description__" --Install="__launcher__" --StartPath="__startpath__" --Classpath=%CP% --StartMode=jvm --StopMode=jvm %START_VAR% %STOP_VAR% %SERVICE_LOGS%
