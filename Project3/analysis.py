import pandas as pd
import matplotlib.pyplot as plt
from kmodes.kmodes import KModes
from numpy.random import default_rng

def k_anonymity():
    # creating k anonymity for species and birth month using k modes clustering
    kanon = villagerDF.copy()
    group_bday(kanon)
    predictors = ["Birthday", "Species", "Gender"]
    # k = find_optimalK(kanon, predictors)
    k = 3
    fit_kmodes(kanon, k, predictors)
    print("Fit", k, "clusters")
    # dropping species and gender identifier and rearranging columns
    kanon.drop('Species', axis=1, inplace=True)
    kanon.drop('Gender', axis=1, inplace=True)
    # rearranging for output
    kanon = kanon[['Cluster', 'Birthday', 'Personality', 'Hobby', 'Catchphrase', 'Favorite Song', 'Unique Entry ID']]
    print(kanon.head(10))
    
# find the optimal K for k modes clustering
def find_optimalK(df, predictors):
    # k modes clustering using birthday and species
    # Elbow curve to find optimal K
    cost = []
    K = range(1,6)
    for num_clusters in list(K):
        kmode = KModes(n_clusters=num_clusters, init="Huang", n_init=1, verbose=1)
        kmode.fit_predict(df[predictors])
        cost.append(kmode.cost_)
    plt.plot(K, cost, 'bx-')
    plt.xlabel('No. of clusters')
    plt.ylabel('Cost')
    plt.title('Elbow Method For Optimal K')
    plt.savefig('./output/optimalK.png')
    # find optimal k based on when the graph stops going from strictly decreasing
    change = 0
    optimal = -1
    for i in range(len(cost)):
        if i > 0:
            curr = cost[i-1]-cost[i]
            if curr < change:
                optimal = i
                break
            change = curr
    return optimal

# fits kmodes on k-clusters on df passed in
def fit_kmodes(df, k, predictors):
    # Building model with 3 clusters
    kmode = KModes(n_clusters=k, init="Huang", n_init=1, verbose=1)
    clusters=kmode.fit_predict(df[predictors])
    print(clusters)
    df.insert(0, "Cluster", clusters, True)

# group bday into 4 groups
def group_bday(df):
    gr1 = "[Jan-Mar]"
    gr2 = "[Apr-Jun]"
    gr3 = "[Jul-Sep]"
    gr4 = "[Oct-Dec]"
    for r in df.index:
        curr = df['Birthday'][r]
        if curr == 'Jan' or curr == 'Feb' or curr == 'Mar':
            df['Birthday'][r] = gr1
        if curr == 'Apr' or curr == 'May' or curr == 'Jun': 
            df['Birthday'][r] = gr2
        if curr == 'Jul' or curr == 'Aug' or curr == 'Sep':
            df['Birthday'][r] = gr3
        if curr == 'Oct' or curr == 'Nov' or curr == 'Dec':
            df['Birthday'][r] = gr4

# extract number of villagers who like song ___
def find_favorite_song(df, song):
    num = 0
    for r in df.index:
        if df['Favorite Song'][r] == song:
            num += 1
    if num == 0:
        return -1
    return num

# extract number of villagers who have hobby ___
def find_favorite_hobby(df, hobby):
    num = 0
    for r in df.index:
        if df['Hobby'][r] == hobby:
            num += 1
    return num

# implementing differential privacy for favorite song by adding Laplacian Noise
def find_favorite_song_diffpriv(df, song):
    # add laplacian noise using Python random generator
    rng = default_rng()
    # find total number of villagers with this favorite song to center the distribution at
    mean = find_favorite_song(df, song)
    if mean == -1:
        return -1
    # add noise
    noise = rng.laplace(mean, 1)
    # turn into percentage
    percentage = round(((noise / num_villagers) * 100), 4)
    return percentage

# creating bargraphs of helpful data
def plot_data():
    # birthday
    # seeing number of birthdays per month
    months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]
    month = dict.fromkeys(months, 0)
    for r in villagerDF.index:
        if villagerDF['Birthday'][r] in month:
            month[villagerDF['Birthday'][r]] += 1
        else:
            month[villagerDF['Birthday'][r]] = 1
    # plotting this data to bargraph
    fig, ax = plt.subplots(figsize=(16,10))
    ax.bar(month.keys(), month.values())
    ax.set_title('Bday density for villagers')
    ax.set_ylabel('Num of birthdays')
    plt.xticks(rotation=90)
    plt.savefig('./output/bday.png')

    # species
    # seeing how many species are in the data
    species = dict()
    for r in villagerDF.index:
        if villagerDF['Species'][r] in species:
            species[villagerDF['Species'][r]] += 1
        else:
            species[villagerDF['Species'][r]] = 1
    # plotting this data to bargraph
    fig, ax = plt.subplots(figsize=(16,10))
    ax.bar(species.keys(), species.values(), color="orange")
    ax.set_title('Species density for villagers')
    ax.set_ylabel('Num of villagers')
    plt.xticks(rotation=90)
    plt.savefig('./output/species.png', dpi=300)

    # creating bar graph of favorite song preferences
    songs = dict()
    for r in villagerDF.index:
        if villagerDF['Favorite Song'][r] in songs:
            songs[villagerDF['Favorite Song'][r]] += 1
        else:
            songs[villagerDF['Favorite Song'][r]] = 1
    fig, ax = plt.subplots(figsize=(16,10))
    ax.bar(songs.keys(), songs.values(), color="green")
    ax.set_title('Favorite song density of villagers')
    ax.set_ylabel('Number of times favorite song')
    plt.xticks(rotation=90)
    plt.savefig('./output/songs.png', dpi=300)

# write to a file the list of species and the corresponding amount of villagers that belong to them
def write_species():
    try:
        f = open("./output/species.txt", "w")
        species = dict()
        for r in villagerDF.index:
            if villagerDF['Species'][r] in species:
                species[villagerDF['Species'][r]] += 1
            else:
                species[villagerDF['Species'][r]] = 1
        species = dict(sorted(species.items()))
        for key in species:
            f.write("%s - %d\n" % (key, species[key]))
        f.close()
    except:
        print("Error creating output file")

##############################################################################################
# reading in villager data csv
try:
    villagerData = pd.read_csv('./data/villagers.csv')
except:
    print("Error reading csv file")
villagerDF = pd.DataFrame(villagerData)
# removing the names column to treat this data set as if it were something released
# removing other unnecessary columns
# for col in villagerDF.columns:
#     print(col)
villagerDF.drop('Name', axis=1, inplace=True)
villagerDF.drop('Wallpaper', axis=1, inplace=True)
villagerDF.drop('Flooring', axis=1, inplace=True)
villagerDF.drop('Furniture List', axis=1, inplace=True)
villagerDF.drop('Filename', axis=1, inplace=True)
villagerDF.drop('Color 1', axis=1, inplace=True)
villagerDF.drop('Color 2', axis=1, inplace=True)
villagerDF.drop('Style 1', axis=1, inplace=True)
villagerDF.drop('Style 2', axis=1, inplace=True)

# num villagers in data frame
num_villagers = len(villagerDF)


# fixing birth date to just be birth month
for r in villagerDF.index:
    bday = villagerDF['Birthday'][r]
    x = bday.split('-')
    villagerDF['Birthday'][r] = x[1]

# villagerDF = villagerDF[['Gender', 'Species', 'Birthday', 'Personality', 'Hobby', 'Catchphrase', 'Favorite Song', 'Unique Entry ID']]
# print(villagerDF.head(10))
k_anonymity()


print("Number of villagers in data frame: ", num_villagers)
num = find_favorite_song(villagerDF,"Go K.K. Rider")
print("Differential Privacy")
print("Standard Query")
print("Num of villagers whose favorite song is 'Go. K.K. Rider' - ", num)
print("Percent of villagers whose favorite song is 'Go. K.K. Rider' - ", round(((find_favorite_song(villagerDF,"Go K.K. Rider") / num_villagers) * 100), 4))
print("Percent of villagers whose favorite song is 'Forest Life' - ", round(((find_favorite_song(villagerDF,"Forest Life") / num_villagers) * 100), 4))
print()
print("Different Privacy Query with added Laplacian Noise")
print("Percent of villagers whose favorite song is 'Go. K.K. Rider' - ", find_favorite_song_diffpriv(villagerDF,"Go K.K. Rider"))
print("Percent of villagers whose favorite song is 'Forest Life' - ", find_favorite_song_diffpriv(villagerDF,"Forest Life"))