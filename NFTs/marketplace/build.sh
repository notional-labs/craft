#!/bin/sh

sudo docker run --rm -v "$(pwd)":/code \
  -e CARGO_TERM_COLOR=always \
  --mount type=volume,source="$(basename "$(pwd)")_cache",target=/code/target \
  --mount type=volume,source=registry_cache,target=/usr/local/cargo/registry \
  cosmwasm/rust-optimizer:0.12.6