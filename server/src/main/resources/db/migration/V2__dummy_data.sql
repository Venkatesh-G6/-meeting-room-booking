-- Dummy Employees
INSERT INTO employees 
  (name, email, department) VALUES
  ('Venkatesh G', 
   'venkatesh@technoidentity.com', 
   'Engineering'),
  ('Priya Sharma', 
   'priya@technoidentity.com', 
   'Design'),
  ('Rahul Kumar', 
   'rahul@technoidentity.com', 
   'Product'),
  ('Anitha Reddy', 
   'anitha@technoidentity.com', 
   'HR'),
  ('Suresh Babu', 
   'suresh@technoidentity.com', 
   'Sales'),
  ('Deepika Nair', 
   'deepika@technoidentity.com', 
   'Marketing'),
  ('Karthik Raja', 
   'karthik@technoidentity.com', 
   'Engineering'),
  ('Meena Iyer', 
   'meena@technoidentity.com', 
   'Finance');

-- Rooms
INSERT INTO rooms 
  (room_name, capacity, location, status) 
  VALUES
  ('Meeting Room A', 10, 'Floor 1', 
   'AVAILABLE'),
  ('Meeting Room B', 8, 'Floor 1', 
   'AVAILABLE'),
  ('Training Room', 20, 'Floor 2', 
   'AVAILABLE'),
  ('Pod 1', 4, 'Floor 2', 'AVAILABLE'),
  ('Pod 2', 4, 'Floor 2', 'AVAILABLE'),
  ('Pod 3', 4, 'Floor 3', 'AVAILABLE'),
  ('Board Room', 15, 'Floor 3', 
   'AVAILABLE'),
  ('Conference Room', 12, 'Floor 1', 
   'NA');
