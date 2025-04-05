baseUrl = "/teks/admin/srp/program/10172631/expectation/10173631/correlation/10173636/vote_number/0/all?type=narrative"
citationUrl = "/teks/admin/srp/program/10172631/expectation/10173631/correlation/10173636/vote_number/0/all?type=narrative&ajax_form=1&_wrapper_format=drupal_ajax"

with open('srp.csv') as f:
    lines = f.read().splitlines()

for line in lines:
    data = line.split(",")
    expectation = data[0]
    correlation = data[1]
    for i in range(2, 10):
        url = f"{expectation}/correlation/{correlation}/vote_number/0/all?type=narrative"
