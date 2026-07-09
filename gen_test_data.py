import json, uuid, random
from datetime import datetime, timedelta

start = datetime(2024, 1, 1)
records = []
cold, hot, day_e, night_e = 1000, 500, 2000, 1000

for i in range(600):
    d = start + timedelta(days=i * 2)
    cold += random.randint(3, 8)
    hot  += random.randint(2, 5)
    day_e += random.randint(10, 25)
    night_e += random.randint(5, 18)
    
    records.append({
        'id': str(uuid.uuid4()),
        'date': int(d.timestamp() * 1000),
        'hotWater': hot,
        'coldWater': cold,
        'dayElectricity': day_e,
        'nightElectricity': night_e,
        'shared': i % 20 == 0
    })

container = {
    'appName': 'CommunalExpenses',
    'version': 1,
    'records': records
}

with open('/Users/aleksandr/AndroidStudioProjects/ComunalExpenses2/test_data_600.json', 'w') as f:
    json.dump(container, f, indent=2)

print(f'Generated {len(records)} records')
print(f'Date range: {records[0]["date"]} -> {records[-1]["date"]}')
print(f'File size: {len(json.dumps(container, indent=2))} chars')
