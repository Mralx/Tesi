#########################################
#        THESIS: RESULTS ANALYSIS       #
#For multi-robot coordination strategies#
#########################################

#-->PREPROCESSING
#1.Input data
strategy_results = read.table("../DirectOptimization/Direct.txt")
reserve_summary = read.table("../Reserve/ReserveSummary.txt")
divide_summary = read.table("../DivideAndConquer/DivideSummary.txt")
buddy_summary = read.table("../BuddySystem/BuddySummary.txt")
utility_summary = read.table("../UtilityBased/UtilitySummary.txt")

#2.Re-naming
dimnames(strategy_results)[[2]] = c("Environment","Team_size","Exp_cycles",
                                    "Cycle_time","Area_explored","Avg_dist_run")

#3.Preprocess data
strategy_results[,5] = as.numeric(gsub(",", ".", as.character(strategy_results[,5])))
strategy_results[,6] = as.numeric(gsub(",", ".", as.character(strategy_results[,6])))
avg_cycle_time = 1000 #round(mean(strategy_results[,4]))
attach(strategy_results)

#-->RESULTS ANALYSIS
#4.Analyse for environments and teams
ENV_COUNT = 6
TEAM_COUNT = 10
time_conv_factor = 1000
strategy_summary = data.frame()
variances = c()
for(i in 1:ENV_COUNT){
  curr_env = strategy_results[Environment==i,]
  
  for(j in c(2:TEAM_COUNT)){
    curr_team = curr_env[curr_env$Team_size==j,]
    
    #4.1.Compute avg_exp_cycles and avg_exp_time 
    avg_exp_cycles = mean(curr_team$Exp_cycles)
    avg_exp_time = avg_exp_cycles*avg_cycle_time/time_conv_factor
    
    #4.2.Compute avg_area_exp
    avg_area_exp = mean(curr_team$Area_explored)
    
    #4.3.Compute avg_dist_run
    avg_dist_run = mean(curr_team$Avg_dist_run)
    
    #4.4.Store the env-team results
    env_team_results = c(i,j,avg_exp_time,avg_area_exp,avg_dist_run)
    strategy_summary = rbind(strategy_summary,env_team_results)
  }
  
  variances = c(variances,var(strategy_summary[,3]))
}
###strategy_summary = read.table("../SideFollower/SideSummary.txt")
strategy_summary = cbind(strategy_summary,reserve_summary[,3],divide_summary[,3],
                         buddy_summary[,3],utility_summary[,3])
dimnames(strategy_summary)[[2]] = c("Environment","Team_size","Avg_exp_time",
                                    "Avg_area_exp","Avg_dist_run",
                                    "Reserve_exp_time","Divide_exp_time",
                                    "Buddy_exp_time","Utility_exp_time")

#5.Plot results
library(plotly)
detach(strategy_results)
ENV_COUNT = 6
attach(strategy_summary)
for(i in 1:ENV_COUNT){
  current_title = paste("Environment",i,paste0("(var = ",round(variances[i]/1000),"sec)"))
  p = plot_ly(strategy_summary, x = ~Team_size[Environment==i], y = ~Avg_exp_time[Environment==i], 
        type = 'scatter', 
        mode = 'lines+markers',
        name = 'Direct optimization',
        line = list(color = 'rgb(255, 187, 0)')) %>%
      add_trace(y = ~Reserve_exp_time[Environment==i], name = 'Reserve', line = list(color = 'rgb(0, 8, 255)', width = 1, dash='dot')) %>%
      add_trace(y = ~Divide_exp_time[Environment==i], name = 'Divide', line = list(color = 'rgb(255, 0, 0)', width = 1, dash='dot')) %>%
      add_trace(y = ~Buddy_exp_time[Environment==i], name = 'Buddy', line = list(color = 'rgb(170, 11, 109)', width = 1, dash='dot')) %>%
      add_trace(y = ~Utility_exp_time[Environment==i], name = 'Utility', line = list(color = 'rgb(6, 160, 8)', width = 1, dash='dot')) %>%
      layout(title = current_title,
              xaxis = list(title = "Team size [number of robots]"),
              yaxis = list (title = "Exploration time [seconds]"))
  print(p)
}
write.table(strategy_summary,'../DirectOptimization/DirectSummary.txt')
detach(strategy_summary)
