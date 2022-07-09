### get offering list
archway query contract-state smart --args '{"get_offerings":{}}'
```json
{
    "data":
    {
        "offerings":
        [
            {
                "id": "5",
                "token_id": "3",
                "list_price":
                {
                    "address": "archway1afndft6gx2avr0xf26s6npz2hfwpxaaw03jf8wzr7lwv5wjfz74s6y77lw",
                    "amount": "50000000"
                },
                "contract_addr": "archway1s7thmtdklpmums7kcdt2r2jckcd62eqy3ewcxs3atcej0vgs2mwqtvdheu",
                "seller": "archway1q35c9uzfvwv052m3k4ffv4vmld6n57uqjm0r2t"
            },
            {
                "id": "8",
                "token_id": "1",
                "list_price":
                {
                    "address": "archway1afndft6gx2avr0xf26s6npz2hfwpxaaw03jf8wzr7lwv5wjfz74s6y77lw",
                    "amount": "100000"
                },
                "contract_addr": "archway1s7thmtdklpmums7kcdt2r2jckcd62eqy3ewcxs3atcej0vgs2mwqtvdheu",
                "seller": "archway1a8dq0wced6q29rppdug7yvk8ek0dsrqwe3hxcz"
            }
        ]
    }
}
```