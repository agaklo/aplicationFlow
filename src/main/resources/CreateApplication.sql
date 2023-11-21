INSERT INTO applications
            (id, name, content, status)
            select '1', 'appName1', 'appContent1', 'CREATED'
            where not exists (select * from applications where id = '1')
            union all
            select '2', 'appName2', 'appContent2', 'VERIFIED'
            where not exists (select * from applications where id = '2')
            union all
            select '3', 'appName3', 'appContent3', 'ACCEPTED'
            where not exists (select * from applications where id = '3')
            union all
            select '4', 'appName4', 'appContent4', 'PUBLISHED'
            where not exists (select * from applications where id = '4')
            union all
            select '5', 'appName5', 'appContent5', 'REJECTED'
            where not exists (select * from applications where id = '5');
