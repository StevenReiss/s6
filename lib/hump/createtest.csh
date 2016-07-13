#! /bin/csh -f


set size = -hhh
set vma = "-vmargs -d64 -Xms16m -Xmx8000m -XX:PermSize=256M -XX:MaxPermSize=512M -XX:-UseGCOverheadLimit"
set vma =


set mthd = "-WT"
# set mthd = "-MM"

set opts = "-inone -snone -lnon -lnoh -lnop -nobinding"
set opts = "-iinde -sta -lnon -lnoh -lnop -nobinding"

set proj = wadi
set dir = "/pro/ivy/javasrc /pro/clime/javasrc /pro/s6/javasrc"
set name = /pro/s6/lib/hump/spr1


set hump = "/pro/wadi/hump/src/hump1.csh"
set hump = "djava -d64 -Xmx9000m edu.brown.cs.wadi.hump.HumpMain"


$hump $size -Debug -l $mthd -m $name.model -d $name.hump -r $name.learn $opts $dir $vma >&! $name.run

