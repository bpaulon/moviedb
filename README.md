# React to Spring Boot using a REDIS DB
 Run and browse:
  http://localhost:801/ 
  http://localhost:801/auto-suggest
  
# Eclipse note:
Remove `main/js` from sources so the application does not get re-deployed when Javascript files change. Make sure the environment variable `ECLIPSE_WORKSPACE` is set for the process running Webpack. The Webpack configuration has a different output file based on this environment variable (see `webpack.config.js`)
