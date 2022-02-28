#!/bin/bash
# Need to also add 250k craft for each DAO member after we get EXP worked out
for word in $(cat genesis.txt); do craftd add-genesis-account $(echo $word) 10000exp --account-type perm-locked; done