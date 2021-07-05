The shipment optimization model depends on multiple parameters:

 - number of unmet demand lower bounds of the form "u \geq a * i + b"
 - the cost for one unit of unmet demand
 - the shipment lead time percentile
 - the method to convert random shipment lead times to deterministic
   lead times in the optimization model

In order to choose a good combination of parameters for the SOM,
in this code we perform a grid search over the parameter space,
and plot the performance where the x-axis is the service level,
the y-axis is the inventory level, and each dot is labeled with the
parameter combination.


Running time:

| input | time |
| ----- | ---- |
| test | 0:45 |
| test-2 | 1:35 |



