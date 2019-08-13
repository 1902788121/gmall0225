package com.atguigu.gmall.search;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallSearchWebApplicationTests {

	@Test
	public void contextLoads() throws IOException {
		FileOutputStream fos = new FileOutputStream(new File("d:/a.json"));

		fos.write("woshijson".getBytes());
	}

}
