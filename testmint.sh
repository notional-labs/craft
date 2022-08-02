craftd tx gov  submit-proposal test_proposal.json --from mykey --fees 200stake --from mykey -y

sleep 5

craftd tx gov vote 5 yes --from mykey --fees 200stake  -y

sleep 20

craftd tx exp spend 10000token --from vuong8 --fees 2000stake -y   


craftd start --rpc.laddr tcp://0.0.0.0:12342 --grpc.address 0.0.0.0:12346 --p2p.laddr tcp://0.0.0.0:12345 --grpc-web.address 0.0.0.0:9095
