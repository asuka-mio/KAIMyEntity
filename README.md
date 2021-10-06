###KAIMyEntity for fabric 1.17.x     
核心功能已经修好了    ![Screenshot_20211005_230534](https://user-images.githubusercontent.com/43900799/136050088-52fd273c-70de-4832-ad93-72d4024574b5.png)

需要自定义的着色器     
你需要把release里的两个glsl着色器放到KAIMyEntity里的Shader文件夹里       
--------------------------      
待修复:     
与canvas模组共用时共用的mv矩阵渲染        
网络通信       
左右手物品显示           
linux下nvidia驱动效率问题      
兼容没有启用光影的sodium&&Iris Shader
启用光影后模型消失应该是iris把framebuffer改了
