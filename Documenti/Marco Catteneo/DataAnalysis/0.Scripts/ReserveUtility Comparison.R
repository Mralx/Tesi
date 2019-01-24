reserve_summary = read.table("../ProactiveReserve/ProactiveReserveSummary.txt")
size_summary = read.table("../ProactiveBuddySystem/ProactiveBuddySummary.txt")
buddy_summary = read.table("../SideFollower/SideSummary.txt")
direct_summary = read.table("../DirectOptimization/DirectSummary.txt")

p = plot_ly(reserve_summary, x = ~reserve_summary$Team_size[Environment==2], y = ~reserve_summary$Avg_exp_time[Environment==1], 
            type = 'scatter', 
            mode = 'lines+markers',
            name = 'Proactive reserve',
            line = list(color = 'rgb(255, 187, 0)')) %>%
  add_trace(y = ~size_summary$Avg_exp_time[Environment==2], name = 'Side follower', line = list(color = 'rgb(0, 8, 255)', width = 1)) %>%
  add_trace(y = ~buddy_summary$Avg_exp_time[Environment==2], name = 'Proactive buddy', line = list(color = 'rgb(255, 0, 0)', width = 1)) %>%
  add_trace(y = ~direct_summary$Avg_exp_time[Environment==2], name = 'Direct optimization', line = list(color = 'rgb(170, 11, 109)', width = 1)) %>%
  layout(title = "Environment 1",
         xaxis = list(title = "Team size [number of robots]"),
         yaxis = list (title = "Exploration time [seconds]"))
print(p)