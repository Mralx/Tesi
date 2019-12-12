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

/**
 *
 * @author Victor
 */
public class SinglePointRendezvousStrategySettings {
    //If false, new RV point is simply the point where explorer turns back to head to RV.
    //Otherwise, we try to pick a better spot (near junctions, in corridors, etc.
    public boolean useImprovedRendezvous;
    //If one of the agents doesn't make it to rendezvous, should we try to head to a secondary location?
    public boolean allowReplanning;
    //When calculating time to RV, should we assume that relay has to reach base station location, 
    //or within range radius of communication range of base station?
    public boolean useSimpleCircleCommModelForBaseRange;
    //Should we explicitly make sure explorer has some time to spend exploring the frontier?
    public boolean giveExplorerMinTimeNearFrontier;
    public boolean attemptExplorationByRelay; //should relay try to explore some frontiers if otherwise it will arrive at RV too early
}
