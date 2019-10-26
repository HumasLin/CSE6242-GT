import http.client
import json
import time
import timeit
import sys
import collections
from pygexf.gexf import *

#
connection = http.client.HTTPConnection('rebrickable.com', 80)

url = "https://rebrickable.com/api/v3/lego/sets/?page_size=500&min_parts=1151&ordering=num_parts"

api_num = sys.argv[1]
api_key = "key {}".format(api_num)

header = {"Authorization": api_key, "Accept": "application/json"}

connection.request("GET", url,"",header)

response = connection.getresponse()

data = response.read().decode()

content = json.loads(data)

i = 0
sets = []
all_sets = []
length = len(content['results'])

while(i < length):
    sets = (content['results'][i]['set_num'], content['results'][i]['name'])
    all_sets.append(sets)
    i = i + 1

i = 0
parts = []
all_parts_all = []
while(i < len(all_sets)):
    sn = all_sets[i][0]
    url = "https://rebrickable.com/api/v3/lego/sets/{}/parts/?page_size=1000".format(sn)    
    connection.request("GET", url,"",header)    
    response = connection.getresponse()
    data = response.read().decode() 
    content = json.loads(data)
    n = 0   
    length = len(content['results'])
    all_parts = []
    while(n < length):
        color = content['results'][n]['color']['rgb']
        part_num = str(content['results'][n]['part']['part_num'])
        quantity = str(content['results'][n]['quantity'])
        part_name = content['results'][n]['part']['name']
        part_nc = part_num + '-' + content['results'][n]['color']['rgb']
        parts = [color, quantity, part_name, part_nc]       
        all_parts.append(parts)
        sorted_all_parts = sorted(all_parts, key = lambda parts:parts[1], reverse = True)
        all_parts = sorted_all_parts[:20]
        n = n + 1
    all_parts_all.append(all_parts)
    i = i + 1

gexf = Gexf("Haomin_Lin","bricks_graph")
graph = gexf.addGraph("undirected","static","bricks_graph")
attr_s = graph.addNodeAttribute('set',type='int',defaultValue='0')
attr_p = graph.addNodeAttribute('part',type='int',defaultValue='0')

i = 0
while(i < len(all_sets)):
    tmp_s = graph.addNode(all_sets[i][0], all_sets[i][1])
    tmp_s.addAttribute(attr_s, '1')
    length = len(all_parts_all[i])
    n = 0
    while(n < length):  
        r = str(int(all_parts_all[i][n][-1][-6:-4], 16))
        g = str(int(all_parts_all[i][n][-1][-4:-2], 16))
        b = str(int(all_parts_all[i][n][-1][-2:], 16))  
        tmp_p = graph.addNode(all_parts_all[i][n][3], all_parts_all[i][n][2], r = r, g = g, b = b)
        tmp_p.addAttribute(attr_p, '1')
        id_number = str(i) + "-" + str(n)
        graph.addEdge(id_number, all_sets[i][0], all_parts_all[i][n][3], weight = all_parts_all[i][n][1])
        n = n + 1
    i = i + 1

output_file = open("bricks_graph.gexf","wb")
gexf.write(output_file)

#


# complete auto grader functions for Q1.1.b,d
def min_parts():
    """
    Returns an integer value
    """
    return 1151

def lego_sets():
    """
    return a list of lego sets.
    this may be a list of any type of values
    but each value should represent one set

    e.g.,
    biggest_lego_sets = lego_sets()
    print(len(biggest_lego_sets))
    > 280
    e.g., len(my_sets)
    """
    return all_sets

def gexf_graph():
    """
    return the completed Gexf graph object
    """
    # you must replace these lines and supply your own graph

    return graph

# complete auto-grader functions for Q1.2.d

def avg_node_degree():
    """
    hardcode and return the average node degree
    (run the function called “Average Degree”) within Gephi
    """
    # you must replace this value with the avg node degree
    return 4.238

def graph_diameter():
    """
    hardcode and return the diameter of the graph
    (run the function called “Network Diameter”) within Gephi
    """
    # you must replace this value with the graph diameter
    return 8

def avg_path_length():
    """
    hardcode and return the average path length
    (run the function called “Avg. Path Length”) within Gephi
    :return:
    """
    # you must replace this value with the avg path length
    return 4.929
