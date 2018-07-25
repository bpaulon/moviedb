# React to Spring Boot using a REDIS DB

#Eclipse note:

Remove `main/js` from sources so the application does not get re-deployed when javascript files change. Make sure the environment variable `ECLIPSE_WORKSPACE`
 is set for the process running webpack. The webpack configuration has a different output file based on this environment variable