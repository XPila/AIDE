cd
cp ByteRoundBuffer.java ShortRoundBuffer.java
busybox sed -i s/Byte/Short/ ShortRoundBuffer.java
busybox sed -i s/byte/short/ ShortRoundBuffer.java
cp ByteRoundBuffer.java FloatRoundBuffer.java
busybox sed -i s/Byte/Float/ FloatRoundBuffer.java
busybox sed -i s/byte/float/ FloatRoundBuffer.java
exit
