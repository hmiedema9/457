tc qdisc add dev s1-eth1 root netem loss 20%
tc qdisc add dev s2-eth2 root netem loss 20%
