from PIL import Image

img1 = Image.open('/Users/hw/.cursor/projects/Users-hw-code-tig-Tigshop-Admin/assets/composer-annotation-812e3dea-5150-4b90-83e2-957b825e7e92.png').convert('RGB')
w, h = img1.size
print("Image 1 colors at h//2:")
print([img1.getpixel((x, h//2)) for x in range(0, w, 50)])

