craftd tx gov  submit-proposal test_proposal.json --from mykey --fees 200stake --from mykey -y

sleep 5

craftd tx gov vote 1 yes --from mykey --fees 200stake  -y