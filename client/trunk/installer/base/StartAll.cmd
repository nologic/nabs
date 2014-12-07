echo "Starting Sieve"
start Sieve

echo "Starting Console"
ping 127.0.0.1 -n 2 -w 1000 > nul
start Console

exit
