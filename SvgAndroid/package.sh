echo "creating directories..."
mkdir com
mkdir com/larvalabs
echo "copying files..." 
cat <<END | while read NAME; do mkdir $NAME; cp $CLS_DIR/$NAME/*.class $NAME/; done
com/larvalabs/svgandroid
END
