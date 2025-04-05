file1 = open('programs.csv', 'r')
file2 = open('acceptors.csv', 'r')
file3 = open('rejectors.csv', 'r')
file4 = open('acceptors_1.csv', 'w+')
file5 = open('rejectors_1.csv', 'w')
Lines1 = file1.readlines()
Lines2 = file2.readlines()
Lines3 = file3.readlines()

count = 0
result = {}
for line in Lines1:
    data = line.strip().split(",")
    if data[1] not in result:
        result[data[1]] = data[2]

for line in Lines2:
    data = line.strip().split(",")
    new_line = line.strip() + "," + result[data[5]] + "\n"
    file4.write(new_line)
    print(new_line)

file1.close()
file2.close()
file3.close()
file4.close()
file5.close()