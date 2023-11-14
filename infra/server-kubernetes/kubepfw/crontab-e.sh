* * * * * /home/ecsimsw/kubepfw-health-check.sh >> /home/ecsimsw/kubepfw.log 2>&1
59 23 * * * rm -rf /home/ecsimsw/kubepfw.log 2>&1 && touch /home/ecsimsw/kubepfw.log
