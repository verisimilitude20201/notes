Video: https://www.youtube.com/watch?v=5Cjt0l2qP7o&list=PLQnljOFTspQXNP6mQchJVP3S-3oKGEuw9&index=68(05:00)
# Why Salesforce services went down

1. DNS was the cause and it's the single point of failure for your infrastructure. Salesforce took 5 hours to bring the entire infrastructure up.

## What happened - Summary
1. The salesforce service disruption impacted the ability for users to login into their Salesforce environments within the core Salesforce services
2. The team had high confidence that it was due to an internal emergency Domain name change.

## Root Cause
1. DNS update triggered DNS services to stop and not restart immediately.
2. This prevented internal services from finding each other