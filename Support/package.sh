echo "creating directories..."
mkdir com
mkdir com/xpila
mkdir com/xpila/support
echo "copying files..." 
cat <<END | while read NAME; do mkdir $NAME; cp $CLS_DIR/$NAME/*.class $NAME/; done
com/xpila/support/activity
com/xpila/support/jni
com/xpila/support/os
com/xpila/support/io
com/xpila/support/nio
com/xpila/support/log
com/xpila/support/console
com/xpila/support/charset
com/xpila/support/view
com/xpila/support/pcm
com/xpila/support/xport
com/xpila/support/xbus
com/xpila/support/display
com/xpila/support/bitmap
com/xpila/support/usb2serial
END
