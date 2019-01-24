#########################################
#        THESIS: RESULTS ANALYSIS       #
#For multi-robot coordination strategies#
#########################################

#-->PREPROCESSING
#1.Input data
reserve_metrics = read.table("../Reserve/ReserveMetrics.txt")
divide_metrics = read.table("../DivideAndConquer/DivideMetrics.txt")
buddy_metrics = read.table("../BuddySystem/BuddyMetrics.txt")
utility_metrics = read.table("../UtilityBased/UtilityMetrics.txt")
preserve_metrics = read.table("../ProactiveReserve/ProactiveReserveMetrics.txt")
strategy_metrics = read.table("../ProactiveBuddySystem/ProactiveBuddyMetrics.txt")

#2.Re-naming
dimnames(reserve_metrics)[[2]] = c("Environment","Agent","Exp_cycle",
                                   "Interference","Availability","Decision_time")
dimnames(divide_metrics)[[2]] = c("Environment","Agent","Exp_cycle",
                                    "Interference","Availability","Decision_time")
dimnames(buddy_metrics)[[2]] = c("Environment","Agent","Exp_cycle",
                                   "Interference","Availability","Decision_time")
dimnames(utility_metrics)[[2]] = c("Environment","Agent","Exp_cycle",
                                    "Interference","Availability","Decision_time")
dimnames(preserve_metrics)[[2]] = c("Environment","Agent","Exp_cycle",
                                   "Interference","Availability","Decision_time")
dimnames(strategy_metrics)[[2]] = c("Environment","Agent","Exp_cycle",
                                   "Interference","Availability","Decision_time")
reserve_metrics = reserve_metrics[-which(reserve_metrics$Availability==0),]
divide_metrics = divide_metrics[-which(divide_metrics$Availability==0),]
buddy_metrics = buddy_metrics[-which(buddy_metrics$Availability==0),]
#utility_metrics = utility_metrics[-which(utility_metrics$Availability==0),]
preserve_metrics = reserve_metrics[-which(preserve_metrics$Availability==0),]
strategy_metrics = strategy_metrics[-which(strategy_metrics$Availability==0),]

reserve_metrics = reserve_metrics[-which(reserve_metrics$Interference==0),]
divide_metrics = divide_metrics[-which(divide_metrics$Interference==0),]
buddy_metrics = buddy_metrics[-which(buddy_metrics$Interference==0),]
#utility_metrics = utility_metrics[-which(utility_metrics$Interference==0),]
preserve_metrics = reserve_metrics[-which(preserve_metrics$Interference==0),]
strategy_metrics = strategy_metrics[-which(strategy_metrics$Interference==0),]

#3.Computing summaries
ENV_COUNT = 6
attach(preserve_metrics)
n = dim(divide_metrics)[[1]]
inter_environments = c()
av_environments = c()
dec_environments = c()
for(i in 1:ENV_COUNT){
  inter_curr_env = sum(Interference[Environment==i])/length(Interference[Environment==i])
  av_curr_env = sum(Availability[Environment==i])/length(Availability[Environment==i])
  dec_curr_env = sum(Decision_time[Environment==i])/length(Decision_time[Environment==i])
  
  inter_environments = c(inter_environments,inter_curr_env)
  av_environments = c(av_environments,av_curr_env)
  dec_environments = c(dec_environments,dec_curr_env)
}
temp_av = av_environments
temp_inter = inter_environments
temp_dec = dec_environments
detach(preserve_metrics)

attach(strategy_metrics)
ENV_COUNT = 6
inter_environments = c()
av_environments = c()
dec_environments = c()
for(i in 1:ENV_COUNT){
  inter_curr_env = sum(Interference[Environment==i])/length(Interference[Environment==i])
  av_curr_env = sum(Availability[Environment==i])/length(Availability[Environment==i])
  dec_curr_env = sum(Decision_time[Environment==i])/length(Decision_time[Environment==i])
  
  inter_environments = c(inter_environments,inter_curr_env)
  av_environments = c(av_environments,av_curr_env)
  dec_environments = c(dec_environments,dec_curr_env)
}
detach(strategy_metrics)

comparison = data.frame(temp_av,temp_inter,temp_dec,
                        av_environments,inter_environments,dec_environments)
dimnames(comparison)[[2]] = c("Availability_A","Interference_A","Decision_A",
                              "Availability_B","Interference_B","Decision_B")
attach(comparison)
p1 = plot_ly(comparison, x = ~c(1,2,3,4,5,6), y = ~Availability_A, 
        type = 'scatter', 
        mode = 'lines+markers',
        name = 'Proactive reserve',
        line = list(color = 'rgb(6, 160, 8)')) %>%
  add_trace(y = ~Availability_B, name = 'Proactive buddy', line = list(color = 'rgb(255, 187, 0)', width = 2)) %>%
  layout(title = "Availability",
         xaxis = list(title = "Environments"),
         yaxis = list (title = "Availability [distance]"))
p2 = plot_ly(comparison, x = ~c(1,2,3,4,5,6), y = ~Interference_A, 
        type = 'scatter', 
        mode = 'lines+markers',
        name = 'Proactive reserve',
        line = list(color = 'rgb(6, 160, 8)')) %>%
  add_trace(y = ~Interference_B, name = 'Proactive buddy', line = list(color = 'rgb(255, 187, 0)', width = 2)) %>%
  layout(title = "Interference",
         xaxis = list(title = "Environments"),
         yaxis = list (title = "Interference [distance]"))
p3 = plot_ly(comparison, x = ~c(1,2,3,4,5,6), y = ~Decision_A/3000, 
             type = 'scatter', 
             mode = 'lines+markers',
             name = 'Proactive reserve',
             line = list(color = 'rgb(6, 160, 8)')) %>%
  add_trace(y = ~Decision_B/3000, name = 'Proactive buddy', line = list(color = 'rgb(255, 187, 0)', width = 2)) %>%
  layout(title = "Decision time",
         xaxis = list(title = "Environments"),
         yaxis = list (title = "Decision time [s]"))
print(p1)
print(p2)
print(p3)
detach(comparison)
