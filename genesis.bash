#!/bin/bash

for word in $(cat genesis.txt); do craftd add-genesis-account $(echo $word) --vesting-amount 10000exp --permanent; done
