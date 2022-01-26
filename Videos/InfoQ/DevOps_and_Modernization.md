Video: https://www.youtube.com/watch?v=f7nYSjCWECw&list=PLndbWGuLoHeYGKisSVu625l-tEtp-zPbU&index=10 (24:00)

# DevOps and Modernization excellence at CSG

## Initial CSG Problems when they began DevOps
1. Growth
2. Lower costs
3. Fast
4. More stability

## Problems with legacy systems
1. How do you patch them?
2. Maintenance
3. Increase stability and safety
4. Grow fast and deliver features
5. Support growth without a massive increase in cost
6. Minimize exposure from dangerous vendors: Dangerous vendors increase your effort and time. Confusing contracts

## Approach to modernization
1. Automated Testing: Prolific modern test coverage
2. Continous integration: Version Control, automated build and deploy. Create fast feedback for change
3. Telemetry: Instrument the code. Make it visible, understand how the production behaves.
4. Infrastructure: Remove proprietary infra. Self service. Infra as code. Cloud.
5. Feature Switching: Add traps to switch between new and old code
6. Code Porting: Retarget the code. 
7. Incremental Rollout: Using feature switces do canary roll outs.
8. Strangulation of legacy technology

## Golf course software
Imagine this: A CEO meeting up a potential customer about a product and talking things like
1. No developers
2. Low code
3. Just map your data
4. Easy to operate
5. Always integrated


## Stories of modernization at CSG
#### API platform and event layer
Existing problems
----------------
1. Poor developer aesthetics: 14 hour builds, 4 weeks of test, low code, point and click.
2. Low density TPS 
3. Unsustainable cost to business growth
4. Poor operation aesthetics: No observability

Approach:
--------
1. Move to commodity stack
2. Port 300 transactions leveraged by 1200 integrations to native code. 
3. Strangulate legacy code: Canaries, feature switches 
4. Foundational modernaization: Infra as code, telemetry, Testing, CI

- Had a software load-balancer with some configuration flags. Low-risk client would still be served with new code making sure everything works. Overtime, everything works and that legacy enterprise bus would be removed.
- Used proprietary test tools used only by testers. It would lead to high cost and increasing manual effort. To correct this, commoditized the tests to gherkin, used tests in version control in code, testers and developers collaborated on test suites. 
- They used routing flags for the test suites. Run the tests with the old code with the old routing flag set, running the tests with new code with new routing flags and comparing the results.

### Mainframes DB2 
Problems
--------
- Lack of commodity data access
- Maintainaibility at risk
- Unsustainable cost jeopardizing viability

Approach
---------
1. Incremental roll out
2. Strangle off VSAM
3. Direct DB2 queries.

Datastore migration pattern
---------------------------
1. Old data store is primary
2. Make the new data store as replica. Compare.
3. Make the new data store as primary and old as replica. Compare
4. New data store is primary. Disconnect old one.

### Mainframe Java
Problems
--------
1. 3.7 million lines of assembly code
2. Maintainaibility problem
3. Costs

Approach:
--------
1. Cross-compilation tooling - Assembly-Java
2. All CIs converted to code coverage
3. Incremental rollout via feature switching
4. Strangle off assembly code


### Composition platform

Problems
-------
1. 4 million lines of propreitary Cobol
2. No functional tests or telemetry
3. No scaling
4. Multiple impacting incidents per day

Approach
--------
1. Modern CI-based unit tests around Cobol


### Misc
1. Release on demand: Features going in when they are done. Short development cycles, roll-back when they are unstable.
2. Get rid of CAB
3. Architect for safety

## Modernization is essential
1. Leverage automated testing.
2. CI
3. Telemetry
4. Optimize for developer and operational aesthetics
5. Code porting
6. Incremental roll-out
7. Feature switches.