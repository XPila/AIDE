rm -r jar
rm support.jar

mkdir jar
mkdir jar/com
mkdir jar/com/xpila
mkdir jar/com/xpila/support
mkdir jar/com/xpila/support/os
mkdir jar/com/xpila/support/io
mkdir jar/com/xpila/support/log
mkdir jar/com/xpila/support/console
mkdir jar/com/xpila/support/view
mkdir jar/com/xpila/support/pcm
mkdir jar/com/xpila/support/xbus
 

cp MANIFEST.MF jar

cp bin/classes2/com/xpila/support/os/*.class jar/com/xpila/support/os

cp bin/classes2/com/xpila/support/io/*.class jar/com/xpila/support/io

cp bin/classes2/com/xpila/support/log/*.class jar/com/xpila/support/log
 
cp bin/classes2/com/xpila/support/console/*.class jar/com/xpila/support/console

cp bin/classes2/com/xpila/support/view/*.class jar/com/xpila/support/view
 
cp bin/classes2/com/xpila/support/pcm/*.class jar/com/xpila/support/pcm

cp bin/classes2/com/xpila/support/xbus/*.class jar/com/xpila/support/xbus

#cd jar
#zip -r ../support.jar *

exit