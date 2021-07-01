#!/bin/bash

brew install mkcert
mkcert -install
mkcert dotcms-local.test
sudo -- sh -c -e "echo '127.0.0.1   dotcms-local.test' >> /etc/hosts";
