/*
 *     Copyright 2010, 2015 Julian de Hoog (julian@dehoog.ca), Victor Spirin (victor.spirin@cs.ox.ac.uk)
 *
 *     This file is part of MRESim 2.2, a simulator for testing the behaviour
 *     of multiple robots exploring unknown environments.
 *
 *     If you use MRESim, I would appreciate an acknowledgement and/or a citation
 *     of our papers:
 *
 *     @inproceedings{deHoog2009,
 *         title = "Role-Based Autonomous Multi-Robot Exploration",
 *         author = "Julian de Hoog, Stephen Cameron and Arnoud Visser",
 *         year = "2009",
 *         booktitle = "International Conference on Advanced Cognitive Technologies and Applications (COGNITIVE)",
 *         location = "Athens, Greece",
 *         month = "November",
 *     }
 *
 *     @incollection{spirin2015mresim,
 *       title={MRESim, a Multi-robot Exploration Simulator for the Rescue Simulation League},
 *       author={Spirin, Victor and de Hoog, Julian and Visser, Arnoud and Cameron, Stephen},
 *       booktitle={RoboCup 2014: Robot World Cup XVIII},
 *       pages={106--117},
 *       year={2015},
 *       publisher={Springer}
 *     }
 *
 *     MRESim is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     MRESim is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along with MRESim.
 *     If not, see <http://www.gnu.org/licenses/>.
 */
package exploration.rendezvous;

import agents.RealAgent;
import config.SimulatorConfig;

/**
 *
 * @author Victor
 */
public class RendezvousStrategyFactory {
    public static IRendezvousStrategy createRendezvousStrategy(SimulatorConfig simConfig, RealAgent agent) {
        if (!simConfig.RVThroughWallsEnabled()) {
            return createSinglePointRendezvousStrategy(simConfig, agent);
        } else {
            MultiPointRendezvousStrategySettings rvSettings = new MultiPointRendezvousStrategySettings();
            rvSettings.moveToBetterCommsWhileWaiting = true;
            rvSettings.SamplePointDensity = simConfig.getSamplingDensity(); //roughly every 20 sq. units
            rvSettings.replanOurMeetingPoint = simConfig.getExploreReplan();
            rvSettings.attemptExplorationByRelay = simConfig.getRelayExplore();
            rvSettings.tryToGetToExplorerRV = simConfig.getTryToGetToExplorerRV();
            rvSettings.useSingleMeetingTime = simConfig.getUseSingleMeetingTime();
            IRendezvousStrategy rendezvousStrategy = new MultiPointRendezvousStrategy(agent, rvSettings);
            return rendezvousStrategy;
        }
    }
    
    public static IRendezvousStrategy createSinglePointRendezvousStrategy(SimulatorConfig simConfig, RealAgent agent) {
        SinglePointRendezvousStrategySettings rvSettings = new SinglePointRendezvousStrategySettings();
        rvSettings.allowReplanning = simConfig.replanningAllowed();
        rvSettings.useImprovedRendezvous = simConfig.useImprovedRendezvous();
        rvSettings.attemptExplorationByRelay = simConfig.getRelayExplore();
        rvSettings.useSimpleCircleCommModelForBaseRange = false;
        rvSettings.giveExplorerMinTimeNearFrontier = true;
        IRendezvousStrategy rendezvousStrategy = new SinglePointRendezvousStrategy(agent, rvSettings);
        return rendezvousStrategy;
    }
    
    public static SinglePointRendezvousStrategy createSinglePointImprovedRendezvousStrategy(RealAgent agent) {
        SinglePointRendezvousStrategySettings rvSettings = new SinglePointRendezvousStrategySettings();
        rvSettings.allowReplanning = false;
        rvSettings.useImprovedRendezvous = true;
        SinglePointRendezvousStrategy rendezvousStrategy = new SinglePointRendezvousStrategy(agent, rvSettings);
        return rendezvousStrategy;
    }
}