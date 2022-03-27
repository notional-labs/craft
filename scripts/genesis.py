def main():
    createAuthAccountsJSON()
    # createBalanceJSON()
    pass


def createAuthAccountsJSON():
    finalAccountOutput = []
    duplicateAccountChecker = {}
    walletsList = """craft1lxh0u07haj646pt9e0l2l4qc3d8htfx5se2ntp
craft1fpsv3uk2tqq362zvh82777gjexjduu79t8z29v
craft1f4sjvl8ujk9g6vtdvratlzmz7k7a5d9dnm325l
craft165qvsanfcnm075vld6r90haypxwe27rjlzw6r7
craft1uc8a9f43fqc4pum8ejfr3n69l87c87r2ja2xwq
craft1nzd8jnu69w8eux3dcg4axxyarm7age86p4pjd4
craft1ray0cavvxaa92xp08affex2casrekddgxt2lcl
craft12sczd7vmheqat355txqducgm6fk86ye4s5lkz8
craft1h2kjnnnryh9ezgzj6yrax4snzfner9qaqjfqr9
craft1pcal3gqemz4g9e6p52had37azx2p9hg64rapfk
craft13t0vcrdlj3vju5pqgwzlyr7lcw9s96kahfgla4
craft1f0043tu4clcs7skhzs7760hw095xzv6tflvvfd
craft18r6j04h3pa49kmhazdqz6plt5t35jxswdv4y93
craft1j8s87w3fctz7nlcqtkl5clnc805r2404eu8xvq
craft1gmgck2kytg9tj60m2c3m9gdaavencp7l77nwd6
craft1s42j67d3f6julvx4nhjgmcxf74xfph26t9vcn2
craft1r8qt0k0t7kywdndjs2udlem4j7m2yk29ua47mm
craft1fasl4wc76fxxxmvkrkzre9cejyn0x2lmgckyqz
craft1hg49kyr022qvj9hq6esvm5g9gtax4c262hutn2
craft1f0l4wt43gyktrveku2aqc3mw9tz3dk9j7nwese
craft14l4g4lvwl0tg6thmpl5q9337drs3he44mr0795
craft1dv3v662kd3pp6pxfagck4zyysas82ads89ldp8
craft1w9rugshphy0a849yp56klt5ul8y55mne7g7vf0
craft1ddd9vf56hv5jntdqkd85dv6je6xes25g3ykyn4
craft145r7j5u2868er6ylj3nt9zzg5lnc9gyt4d6282
craft1me3g0a2nr24sjykmhvpl687f6zt66nlvhv0y9h
craft1ugjgu3hg7jcmafq3tr6g857950vuyj0kua0hka"""

    for idx, wallet in enumerate(walletsList.split("\n")):
        if(len(wallet) < 10):
            continue    

        if wallet in duplicateAccountChecker.keys():
            continue
        else:
            duplicateAccountChecker[wallet] = True

        finalAccountOutput.append(f"""        {{
            "@type": "/cosmos.vesting.v1beta1.PermanentLockedAccount",
            "base_vesting_account": {{
                "base_account": {{
                "address": "{wallet}",
                "pub_key": null,
                "account_number": "{idx}",
                "sequence": "0"
                }},
                "original_vesting": [
                {{
                    "denom": "exp",
                    "amount": "10000"
                }}
                ],
                "delegated_free": [],
                "delegated_vesting": [],
                "end_time": "-1"
            }}
        }}""")

    print(",\n".join(finalAccountOutput))



def createBalanceJSON():
    finalBalanceOutput = []
    duplicateAccountChecker = {}

    totalEXPAmount = 0

    for idx, wallet in enumerate(walletsList.split("\n")):
        if(len(wallet) < 10):
            continue    

        if wallet in duplicateAccountChecker.keys():
            continue
        else:
            duplicateAccountChecker[wallet] = True

        totalEXPAmount += 10000
        finalBalanceOutput.append(f"""        {{
          "address": "{wallet}",
          "coins": [
            {{
              "denom": "exp",
              "amount": "10000"
            }}
          ]
        }}""")

    print(",\n".join(finalBalanceOutput))
    print(f"""
      ],
      "supply": [
        {{
          "denom": "exp",
          "amount": "{totalEXPAmount}"
        }}
      ],""")

if __name__ == "__main__":
    main()