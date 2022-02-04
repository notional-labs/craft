#!/bin/bash

for word in $(cat genesis.txt); do craftd add-genesis-account $(echo $word) --vesting-amount 10000exp --account-type perm-locked; done
