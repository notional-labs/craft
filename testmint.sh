craftd tx gov  submit-proposal test_proposal.json --from mykey --fees 200stake --from mykey -y

sleep 5

craftd tx gov vote 4 yes --from mykey --fees 200stake  -y

sleep 20

craftd tx exp spend 1000token --from vuong2 --fees 2000stake -y                                                                                                                                                                                   