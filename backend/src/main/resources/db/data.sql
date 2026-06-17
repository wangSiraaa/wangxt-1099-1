INSERT INTO sys_user (user_code, user_name, password, role_type, phone) VALUES
('SHIPPER001', '华东发货仓', '123456', 'SHIPPER', '13800000001'),
('SHIPPER002', '华南发货仓', '123456', 'SHIPPER', '13800000002'),
('CARRIER001', '顺丰物流', '123456', 'CARRIER', '13900000001'),
('CARRIER002', '圆通速递', '123456', 'CARRIER', '13900000002'),
('FINANCE001', '财务主管', '123456', 'FINANCE', '13700000001'),
('ADMIN001', '系统管理员', '123456', 'FINANCE', '13700000002');

INSERT INTO pallet (pallet_code, pallet_name, specification, deposit_amount, status) VALUES
('P001', '标准木托盘', '1200x1000x150mm', 100.00, 'AVAILABLE'),
('P002', '标准木托盘', '1200x1000x150mm', 100.00, 'AVAILABLE'),
('P003', '标准木托盘', '1200x1000x150mm', 100.00, 'AVAILABLE'),
('P004', '标准木托盘', '1200x1000x150mm', 100.00, 'AVAILABLE'),
('P005', '标准木托盘', '1200x1000x150mm', 100.00, 'AVAILABLE'),
('P006', '塑料托盘', '1200x1000x160mm', 150.00, 'AVAILABLE'),
('P007', '塑料托盘', '1200x1000x160mm', 150.00, 'AVAILABLE'),
('P008', '塑料托盘', '1200x1000x160mm', 150.00, 'AVAILABLE'),
('P009', '重型托盘', '1400x1200x180mm', 200.00, 'AVAILABLE'),
('P010', '重型托盘', '1400x1200x180mm', 200.00, 'AVAILABLE');

INSERT INTO account_period (period_code, period_name, start_date, end_date, status) VALUES
('2026-06', '2026年6月', '2026-06-01', '2026-06-30', 'OPEN'),
('2026-05', '2026年5月', '2026-05-01', '2026-05-31', 'OPEN'),
('2026-04', '2026年4月', '2026-04-01', '2026-04-30', 'CLOSED');

INSERT INTO deposit_balance (shipper_id, shipper_name, total_paid, total_deducted, total_refunded, current_balance, frozen_amount, available_amount) VALUES
(1, '华东发货仓', 5000.00, 200.00, 0.00, 4800.00, 0.00, 4800.00),
(2, '华南发货仓', 3000.00, 0.00, 0.00, 3000.00, 0.00, 3000.00);

INSERT INTO deposit_payment (payment_no, shipper_id, shipper_name, payment_amount, payment_date, period_id, status, payment_method) VALUES
('DP20260601001', 1, '华东发货仓', 5000.00, '2026-06-01', 1, 'CONFIRMED', '银行转账'),
('DP20260602001', 2, '华南发货仓', 3000.00, '2026-06-02', 1, 'CONFIRMED', '银行转账');
