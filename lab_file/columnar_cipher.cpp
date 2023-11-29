#include <algorithm>
#include <iostream>
#include <string>
#include <unordered_map>
#include <vector>
using namespace std;

class ColumnarCipher {
  string inputText;
  string key;
  vector<vector<char>> matrix;

 public:
  void setKey() {
    cout << "Enter Key : ";
    cin >> key;
  }

  void setInputText() {
    cout << "Enter input text : ";
    cin.ignore();
    getline(cin, inputText);
  }

  int search(char a, vector<char> &temp) {
    for (int i = 0; i < temp.size(); i++) {
      if (temp[i] == a) {
        temp[i] = ' ';
        return (i + 1);
      }
    }
    return 0;
  }

  string encrypt() {
    unordered_map<int, int> index;
    int columnCount = key.length();
    vector<char> temp;
    for (int i = 0; i < columnCount; i++) {
      temp.push_back(key[i]);
    }
    matrix.push_back(temp);
    vector<char> temp2;
    sort(temp.begin(), temp.end());
    for (int i = 0; i < columnCount; i++) {
      char inp = search(matrix[0][i], temp) + '0';
      index[inp - '0'] = i;
      temp2.push_back(inp);
    }
    matrix.push_back(temp2);
    int j = 0;
    temp2.clear();
    for (int i = 0; inputText[i] != '\0'; i++) {
      if (inputText[i] == ' ')
        temp2.push_back('$');
      else
        temp2.push_back(inputText[i]);
      j++;
      if (j == columnCount) {
        matrix.push_back(temp2);
        temp2.clear();
        j = 0;
      }
    }
    if (temp2.size()) matrix.push_back(temp2);

    for (int i = 0; i < matrix.size(); i++) {
      for (int j = 0; j < matrix[0].size(); j++) {
        cout << matrix[i][j] << " ";
      }
      cout << endl;
    }
    string encryptedText = "";
    for (int i = 1; i <= columnCount; i++) {
      for (int j = 2; j < matrix.size(); j++) {
        if (matrix[j][index[i]] != '$') encryptedText += matrix[j][index[i]];
      }
      encryptedText += ' ';
    }
    return encryptedText;
  }

  void decrypt() {
    string decryptedText;
    for (int i = 2; i < matrix.size(); i++) {
      for (int j = 0; j < matrix[i].size(); j++) {
        if (matrix[i][j] == '$')
          decryptedText += ' ';
        else
          decryptedText += matrix[i][j];
      }
    }
    cout << "Decrypted Text: " << decryptedText << endl;
  }
};

int main() {
  ColumnarCipher cipher;
  int option;
  bool flag = true;
  while (flag) {
    cout << "Select : " << endl;
    cout << "1: To enter input text" << endl;
    cout << "2: To enter key " << endl;
    cout << "3: To encrypt " << endl;
    cout << "4: To decrypt " << endl;
    cout << "5: To Exit " << endl;
    cin >> option;
    if (option == 1) {
      cipher.setInputText();
    }
    if (option == 2) {
      cipher.setKey();
    }
    if (option == 3) {
      string encryptedText = cipher.encrypt();
      cout << endl;
      cout << "Encrypted Text: " << encryptedText << endl;
    }
    if (option == 4) {
      cipher.decrypt();
    }
    if (option == 5) {
      break;
    }
  }
  return 0;
}