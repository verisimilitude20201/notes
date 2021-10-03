Video: 

# On Demand TLS (New feature of Caddy)

1. Consider that you have sent a DNS entry request to map your domains to your IP address and it's still not ready and you want to generate the Web certificate. Imagine that you own 10 domain entries pointing to the same IP address through Server Name indication (SNI) ?

2. Caddy invented On-Demand TLS. Caddy obtains the certificate for you from Let's encrypt for the first time. Caddy realizes that the DNS entry is live on the first TCP request to the domain website and it asyncly fetches the certificate from Let's encrypt, it redirects to https once done, updates the HSTS list and it will be a TLS-encrypted connection. The first request will be slow.

3. If you have many sub-domains this is actually slower process because you can just obtain a *.husseinnasser.com certificate from Let's encrypt

4. In case of an error, Caddy will try exponentially (Exponential back-off) to generate the certificate.