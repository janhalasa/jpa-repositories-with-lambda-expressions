INSERT INTO Vendor (id, name, version) VALUES (1000, 'Renault', 1);
INSERT INTO Vendor (id, name, version) VALUES (1001, 'Toyota', 1);
INSERT INTO Vendor (id, name, version) VALUES (1002, 'Volkswagen', 1);
INSERT INTO Vendor (id, name, version) VALUES (1003, 'Ford', 1);
INSERT INTO Vendor (id, name, version) VALUES (1004, 'Škoda', 1);
INSERT INTO Vendor (id, name, version) VALUES (1005, 'Fiat', 1);
INSERT INTO Vendor (id, name, version) VALUES (1006, 'Mazda', 1);
INSERT INTO Vendor (id, name, version) VALUES (1007, 'Honda', 1);
INSERT INTO Vendor (id, name, version) VALUES (1008, 'Mercedes', 1);
INSERT INTO Vendor (id, name, version) VALUES (1009, 'BMW', 1);
INSERT INTO Vendor (id, name, version) VALUES (1010, 'Peugeot', 1);
INSERT INTO Vendor (id, name, version) VALUES (1011, 'Tesla', 1);
INSERT INTO Vendor (id, name, version) VALUES (1012, 'Volvo', 1);
INSERT INTO Vendor (id, name, version) VALUES (1013, 'Zastava', 1);
INSERT INTO Vendor (id, name, version) VALUES (1014, 'Dacia', 1);
INSERT INTO Vendor (id, name, version) VALUES (1015, 'Seat', 1);

INSERT INTO CarModel (id, vendor_id, name) VALUES (1, 1000, 'Megane');
INSERT INTO CarModel (id, vendor_id, name) VALUES (2, 1000, 'Espace');
INSERT INTO CarModel (id, vendor_id, name) VALUES (3, 1000, 'Clio');
INSERT INTO CarModel (id, vendor_id, name) VALUES (4, 1014, 'Duster');

INSERT INTO Car (id, model_id, color) VALUES (1, 1, 'red');
INSERT INTO Car (id, model_id, color) VALUES (2, 1, 'green');
INSERT INTO Car (id, model_id, color) VALUES (3, 1, 'blue');
INSERT INTO Car (id, model_id, color) VALUES (4, 2, 'blue');
INSERT INTO Car (id, model_id, color) VALUES (5, 2, 'green');
INSERT INTO Car (id, model_id, color) VALUES (6, 3, 'green');

INSERT INTO ManufacturingPlant (id, name, country) VALUES (1, 'RENAULT-MP1', 'France');
INSERT INTO ManufacturingPlant (id, name, country) VALUES (2, 'RENAULT-MP2', 'Croatia');
