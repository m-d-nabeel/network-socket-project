#include <iostream>
#include <string>
using namespace std;

int main() {
  int key;
  string inputString;
  cout << "Enter the key : ";
  cin >> key;
  cout << "key is : " << key << endl;
  cout << "Enter the String : ";
  cin >> inputString;
  cout << "String is : " << inputString << endl;

  key = key % 26;
  string encryptedString = "";

  for (int i = 0; i < inputString.length(); i++) {
    if (inputString[i] + key < 123) {
      encryptedString += static_cast<char>(inputString[i] + key);
    } else {
      encryptedString +=
          static_cast<char>(inputString[i] + key - 1 - 'z' + 'a');
    }
  }
  cout << "Encrypted String is : " << encryptedString << endl;

  string decryptedString = "";
  for (int i = 0; i < encryptedString.length(); i++) {
    if (encryptedString[i] - key > 96) {
      decryptedString += static_cast<char>(encryptedString[i] - key);
    } else {
      decryptedString +=
          static_cast<char>('z' - ('a' - (encryptedString[i] - key) - 1));
    }
  }
  cout << "Decrypted String is : " << decryptedString << endl;
}