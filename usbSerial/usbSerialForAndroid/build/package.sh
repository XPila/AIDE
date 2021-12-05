echo "creating directories..."
mkdir com
mkdir com/hoho
mkdir com/hoho/android
mkdir com/hoho/android/usbserial
echo "copying files..." 
cat <<END | while read NAME; do mkdir $NAME; cp $CLS_DIR/$NAME/*.class $NAME/; done
com/hoho/android/usbserial/driver
com/hoho/android/usbserial/util
END
