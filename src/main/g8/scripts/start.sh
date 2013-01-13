#!/usr/bin/env sh
exec /opt/jdk1.7.0/bin/java $* -cp "`dirname $0`/lib/*" -Dconfig.file="conf/application.conf" -Djava.net.preferIPv4Stack=true -Dhttp.port=80 play.core.server.NettyServer `dirname $0