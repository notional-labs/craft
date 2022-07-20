import asyncio
from MinePI import render_3d_skin # pip install MinePI
import requests
import shutil
from PIL import Image
import os


current_dir = os.path.dirname(os.path.realpath(__file__))
os.makedirs("skin_renderings", exist_ok=True)
current_dir = os.path.join(current_dir, "skin_renderings")

async def main(url=""):
    # download 
    # url = 
    filename = url.split("/")[-1] + ".png"
    file_path = current_dir + "/" + filename

    r = requests.get(url, stream = True)
    if r.status_code == 200:
        # Set decode_content value to True, otherwise the downloaded image file's size will be zero.
        r.raw.decode_content = True
        # Open a local file with wb ( write binary ) permission.
        with open(file_path,'wb') as f:
            shutil.copyfileobj(r.raw, f)            
        print('Image successfully Downloaded: ',filename)
    else:
        print('Image Couldn\'t be retrieved')

    skin_image = Image.open(file_path)
    image = await render_3d_skin(skin_image=skin_image, hr=30, vr=-20, aa=True,
        vrll=20, vrrl=-20, vrla=-25, vrra=25) # walking motion
    
    # show an Image.Image
    # image.show()
    # delete file filename
    os.remove(file_path)
    # save image to the filename
    image.save(file_path)

    # we then upload this image -> ipfs / our server so it can be rendered out

skins = [
    "http://textures.minecraft.net/texture/47bd2432e9ebfb2341b3c57846ab66f04137c9ad2d734eb6d8f66555813a4fab",
    "http://textures.minecraft.net/texture/b8a58d54365a88b813b4287962020f45816d031f2334334448477de8856e7e92"
]
for skin in skins:
    asyncio.run(main(skin))